/*
 * Copyright (c) Kuba Szczodrzyński 2020-4-16.
 */

package pl.szczodrzynski.edziennik.data.api.edziennik.vulcan.login

import pl.szczodrzynski.edziennik.App
import pl.szczodrzynski.edziennik.data.api.*
import pl.szczodrzynski.edziennik.data.api.edziennik.vulcan.DataVulcan
import pl.szczodrzynski.edziennik.data.api.edziennik.vulcan.data.VulcanWebMain
import pl.szczodrzynski.edziennik.data.api.models.ApiError
import pl.szczodrzynski.edziennik.getString
import pl.szczodrzynski.edziennik.isNotNullNorEmpty
import pl.szczodrzynski.fslogin.FSLogin
import pl.szczodrzynski.fslogin.realm.CufsRealm

class VulcanLoginWebMain(val data: DataVulcan, val onSuccess: () -> Unit) {
    companion object {
        private const val TAG = "VulcanLoginWebMain"
    }

    private val web by lazy { VulcanWebMain(data, null) }

    init { run {
        copyFromLoginStore()

        if (data.profile != null && data.isWebMainLoginValid()) {
            onSuccess()
        }
        else {
            if (data.symbol.isNotNullNorEmpty()
                    && data.webType.isNotNullNorEmpty()
                    && data.webHost.isNotNullNorEmpty()
                    && (data.webEmail.isNotNullNorEmpty() || data.webUsername.isNotNullNorEmpty())
                    && data.webPassword.isNotNullNorEmpty()) {
                try {
                    val success = loginWithCredentials()
                    if (!success)
                        data.error(ApiError(TAG, ERROR_VULCAN_WEB_DATA_MISSING))
                } catch (e: Exception) {
                    data.error(ApiError(TAG, EXCEPTION_VULCAN_WEB_LOGIN)
                            .withThrowable(e))
                }
            }
            else {
                data.error(ApiError(TAG, ERROR_LOGIN_DATA_MISSING))
            }
        }
    }}

    private fun copyFromLoginStore() {
        data.loginStore.data.apply {
            // 4.0 - new login form - copy user input to profile
            if (has("symbol")) {
                data.symbol = getString("symbol")
                remove("symbol")
            }
        }
    }

    private fun loginWithCredentials(): Boolean {
        val realm = when (data.webType) {
            "cufs" -> CufsRealm(
                    host = data.webHost ?: return false,
                    symbol = data.symbol ?: "default",
                    httpCufs = data.webIsHttpCufs
            )
            "adfs" -> CufsRealm(
                    host = data.webHost ?: return false,
                    symbol = data.symbol ?: "default",
                    httpCufs = data.webIsHttpCufs
            ).toAdfsRealm(id = data.webAdfsId ?: return false)
            "adfslight" -> CufsRealm(
                    host = data.webHost ?: return false,
                    symbol = data.symbol ?: "default",
                    httpCufs = data.webIsHttpCufs
            ).toAdfsLightRealm(
                    id = data.webAdfsId ?: return false,
                    domain = data.webAdfsDomain ?: "adfslight",
                    isScoped = data.webIsScopedAdfs
            )
            else -> return false
        }

        val fsLogin = FSLogin(data.app.http, debug = App.debugMode)
        fsLogin.performLogin(
                realm = realm,
                username = data.webUsername ?: data.webEmail ?: return false,
                password = data.webPassword ?: return false,
                onSuccess = { certificate ->
                    web.saveCertificate(certificate.wresult)

                    // auto-post certificate when not first login
                    if (data.profile != null && data.symbol != null && data.symbol != "default") {
                        val result = web.postCertificate(certificate.wresult, data.symbol ?: "default") { _, state ->
                            when (state) {
                                VulcanWebMain.STATE_SUCCESS -> {
                                    web.getStartPage { _, _ -> onSuccess() }
                                }
                                VulcanWebMain.STATE_NO_REGISTER -> data.error(ApiError(TAG, ERROR_VULCAN_WEB_NO_REGISTER))
                                VulcanWebMain.STATE_LOGGED_OUT -> data.error(ApiError(TAG, ERROR_VULCAN_WEB_LOGGED_OUT))
                            }
                        }
                        // postCertificate returns false if the cert is not valid anymore
                        if (!result) {
                            data.error(ApiError(TAG, ERROR_VULCAN_WEB_CERTIFICATE_EXPIRED)
                                    .withApiResponse(certificate.wresult))
                        }
                    }
                    else {
                        // first login - succeed immediately
                        onSuccess()
                    }
                },
                onFailure = { errorText ->
                    // TODO
                    data.error(ApiError(TAG, 0).withThrowable(RuntimeException(errorText)))
                }
        )

        return true
    }
}
