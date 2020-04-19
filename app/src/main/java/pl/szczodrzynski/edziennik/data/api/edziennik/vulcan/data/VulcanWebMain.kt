/*
 * Copyright (c) Kuba Szczodrzyński 2020-4-17.
 */

package pl.szczodrzynski.edziennik.data.api.edziennik.vulcan.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import im.wangchao.mhttp.Request
import im.wangchao.mhttp.Response
import im.wangchao.mhttp.callback.TextCallbackHandler
import pl.droidsonroids.jspoon.Jspoon
import pl.szczodrzynski.edziennik.data.api.*
import pl.szczodrzynski.edziennik.data.api.edziennik.vulcan.DataVulcan
import pl.szczodrzynski.edziennik.data.api.edziennik.vulcan.login.CufsCertificate
import pl.szczodrzynski.edziennik.data.api.models.ApiError
import pl.szczodrzynski.edziennik.get
import pl.szczodrzynski.edziennik.isNotNullNorBlank
import pl.szczodrzynski.edziennik.utils.Utils
import pl.szczodrzynski.edziennik.utils.models.Date
import java.io.File
import java.net.HttpURLConnection

open class VulcanWebMain(open val data: DataVulcan, open val lastSync: Long?) {
    companion object {
        const val TAG = "VulcanWebMain"
        const val WEB_MAIN = 0
        const val WEB_OLD = 1
        const val WEB_NEW = 2
        const val WEB_MESSAGES = 3
        const val STATE_SUCCESS = 0
        const val STATE_NO_REGISTER = 1
        const val STATE_LOGGED_OUT = 2
    }

    val profileId
        get() = data.profile?.id ?: -1

    val profile
        get() = data.profile

    private val certificateAdapter by lazy {
        Jspoon.create().adapter(CufsCertificate::class.java)
    }

    fun saveCertificate(certificate: String) {
        val file = File(data.app.filesDir, "cert_"+(data.webUsername ?: data.webEmail)+".xml")
        file.writeText(certificate)
    }

    fun readCertificate(): String? {
        val file = File(data.app.filesDir, "cert_"+(data.webUsername ?: data.webEmail)+".xml")
        if (file.canRead())
            return file.readText()
        return null
    }

    fun parseCertificate(certificate: String): CufsCertificate {
        val xml = certificate
                .replace("<[a-z]+?:".toRegex(), "<")
                .replace("</[a-z]+?:".toRegex(), "</")
                .replace("\\sxmlns.*?=\".+?\"".toRegex(), "")

        return certificateAdapter.fromHtml(xml)
    }

    fun postCertificate(certificate: String, symbol: String, onResult: (symbol: String, state: Int) -> Unit): Boolean {
        val cufsCertificate = parseCertificate(certificate)

        // check if the certificate is valid
        if (Date.fromIso(cufsCertificate.expiryDate) < System.currentTimeMillis())
            return false

        val callback = object : TextCallbackHandler() {
            override fun onSuccess(text: String?, response: Response?) {
                if (response?.headers()?.get("Location")?.contains("LoginEndpoint.aspx") == true
                        || response?.headers()?.get("Location")?.contains("?logout=true") == true) {
                    onResult(symbol, STATE_LOGGED_OUT)
                    return
                }
                if (text?.contains("LoginEndpoint.aspx?logout=true") == true) {
                    onResult(symbol, STATE_NO_REGISTER)
                    return
                }
                if (!validateCallback(text, response, jsonResponse = false)) {
                    return
                }
                onResult(symbol, STATE_SUCCESS)
            }

            override fun onFailure(response: Response?, throwable: Throwable?) {
                data.error(ApiError(TAG, ERROR_REQUEST_FAILURE)
                        .withResponse(response)
                        .withThrowable(throwable))
            }
        }

        Request.builder()
                .url("https://uonetplus.${data.webHost}/$symbol/LoginEndpoint.aspx")
                .withClient(data.app.httpLazy)
                .userAgent(SYSTEM_USER_AGENT)
                .post()
                .addParameter("wa", "wsignin1.0")
                .addParameter("wctx", cufsCertificate.targetUrl)
                .addParameter("wresult", certificate)
                .allowErrorCode(HttpURLConnection.HTTP_BAD_REQUEST)
                .allowErrorCode(HttpURLConnection.HTTP_FORBIDDEN)
                .allowErrorCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .allowErrorCode(HttpURLConnection.HTTP_UNAVAILABLE)
                .allowErrorCode(429)
                .callback(callback)
                .build()
                .enqueue()

        return true
    }

    fun getStartPage(symbol: String = data.symbol ?: "default", postErrors: Boolean = true, onSuccess: (html: String, schoolSymbols: List<String>) -> Unit) {
        val callback = object : TextCallbackHandler() {
            override fun onSuccess(text: String?, response: Response?) {
                if (!validateCallback(text, response, jsonResponse = false) || text == null) {
                    return
                }

                if (postErrors) {
                    when {
                        text.contains("status absolwenta") -> ERROR_VULCAN_WEB_GRADUATE_ACCOUNT
                        else -> null
                    }?.let { errorCode ->
                        data.error(ApiError(TAG, errorCode)
                                .withResponse(response)
                                .withApiResponse(text))
                        return
                    }
                }

                data.webPermissions = Regexes.VULCAN_WEB_PERMISSIONS.find(text)?.let { it[1] }

                val schoolSymbols = mutableListOf<String>()
                val clientUrl = "https://uonetplus-uczen.${data.webHost}/$symbol/"
                var clientIndex = text.indexOf(clientUrl)
                var count = 0
                while (clientIndex != -1 && count < 100) {
                    val startIndex = clientIndex + clientUrl.length
                    val endIndex = text.indexOf('/', startIndex = startIndex)
                    val schoolSymbol = text.substring(startIndex, endIndex)
                    schoolSymbols += schoolSymbol
                    clientIndex = text.indexOf(clientUrl, startIndex = endIndex)
                    count++
                }

                onSuccess(text, schoolSymbols)
            }

            override fun onFailure(response: Response?, throwable: Throwable?) {
                data.error(ApiError(TAG, ERROR_REQUEST_FAILURE)
                        .withResponse(response)
                        .withThrowable(throwable))
            }
        }

        Request.builder()
                .url("https://uonetplus.${data.webHost}/$symbol/Start.mvc/Index")
                .userAgent(SYSTEM_USER_AGENT)
                .get()
                .allowErrorCode(HttpURLConnection.HTTP_BAD_REQUEST)
                .allowErrorCode(HttpURLConnection.HTTP_FORBIDDEN)
                .allowErrorCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .allowErrorCode(HttpURLConnection.HTTP_UNAVAILABLE)
                .allowErrorCode(429)
                .callback(callback)
                .build()
                .enqueue()
    }

    private fun validateCallback(text: String?, response: Response?, jsonResponse: Boolean = true): Boolean {
        if (text == null) {
            data.error(ApiError(TAG, ERROR_RESPONSE_EMPTY)
                    .withResponse(response))
            return false
        }

        if (response?.code() !in 200..302 || (jsonResponse && !text.startsWith("{"))) {
            when {
                text.contains("The custom error module") -> ERROR_VULCAN_WEB_429
                else -> ERROR_VULCAN_WEB_OTHER
            }.let { errorCode ->
                data.error(ApiError(TAG, errorCode)
                        .withApiResponse(text)
                        .withResponse(response))
                return false
            }
        }

        val cookies = data.app.cookieJar.getAll(data.webHost ?: "vulcan.net.pl")
        val authCookie = cookies["EfebSsoAuthCookie"]
        if ((authCookie == null || authCookie == "null") && data.webAuthCookie != null) {
            data.app.cookieJar.set(data.webHost ?: "vulcan.net.pl", "EfebSsoAuthCookie", data.webAuthCookie)
        }
        else if (authCookie.isNotNullNorBlank() && authCookie != "null" && authCookie != data.webAuthCookie) {
            data.webAuthCookie = authCookie
        }
        return true
    }

    fun webGetJson(
            tag: String,
            webType: Int,
            endpoint: String,
            method: Int = POST,
            parameters: Map<String, Any> = emptyMap(),
            onSuccess: (json: JsonObject, response: Response?) -> Unit
    ) {
        val url = "https://" + when (webType) {
            WEB_MAIN -> "uonetplus"
            WEB_OLD -> "uonetplus-opiekun"
            WEB_NEW -> "uonetplus-uczen"
            WEB_MESSAGES -> "uonetplus-uzytkownik"
            else -> "uonetplus"
        } + ".${data.webHost}/${data.symbol}/$endpoint"

        Utils.d(tag, "Request: Vulcan/WebMain - $url")

        val payload = JsonObject()
        parameters.map { (name, value) ->
            when (value) {
                is JsonObject -> payload.add(name, value)
                is JsonArray -> payload.add(name, value)
                is String -> payload.addProperty(name, value)
                is Int -> payload.addProperty(name, value)
                is Long -> payload.addProperty(name, value)
                is Float -> payload.addProperty(name, value)
                is Char -> payload.addProperty(name, value)
                is Boolean -> payload.addProperty(name, value)
            }
        }

        val callback = object : TextCallbackHandler() {
            override fun onSuccess(text: String?, response: Response?) {
                if (!validateCallback(text, response))
                    return

                try {
                    val json = JsonParser().parse(text).asJsonObject
                    onSuccess(json, response)
                } catch (e: Exception) {
                    data.error(ApiError(tag, EXCEPTION_VULCAN_WEB_REQUEST)
                            .withResponse(response)
                            .withThrowable(e)
                            .withApiResponse(text))
                }
            }

            override fun onFailure(response: Response?, throwable: Throwable?) {
                data.error(ApiError(tag, ERROR_REQUEST_FAILURE)
                        .withResponse(response)
                        .withThrowable(throwable))
            }
        }

        Request.builder()
                .url(url)
                .userAgent(SYSTEM_USER_AGENT)
                .apply {
                    when (method) {
                        GET -> get()
                        POST -> post()
                    }
                }
                .setJsonBody(payload)
                .allowErrorCode(429)
                .callback(callback)
                .build()
                .enqueue()
    }
}
