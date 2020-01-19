/*
 * Copyright (c) Kuba Szczodrzyński 2019-11-27.
 */

package pl.szczodrzynski.edziennik.config.utils

import android.content.Context
import pl.szczodrzynski.edziennik.App
import pl.szczodrzynski.edziennik.BuildConfig
import pl.szczodrzynski.edziennik.HOUR
import pl.szczodrzynski.edziennik.MainActivity
import pl.szczodrzynski.edziennik.config.Config
import pl.szczodrzynski.edziennik.config.ConfigGrades.Companion.ORDER_BY_DATE_DESC

class ConfigMigration(app: App, config: Config) {
    init { config.apply {

        val p = app.getSharedPreferences("pl.szczodrzynski.edziennik_profiles", Context.MODE_PRIVATE)
        if (p.contains("app.appConfig.appTheme")) {
            // migrate appConfig from app version 3.x and lower.
            // Updates dataVersion to level 2.
            AppConfigMigrationV3(p, config)
        }

        if (dataVersion < 2) {
            appVersion = BuildConfig.VERSION_CODE
            loginFinished = false
            ui.language = null
            ui.theme = 1
            ui.appBackground = null
            ui.headerBackground = null
            ui.miniMenuVisible = false
            ui.miniMenuButtons = listOf(
                    MainActivity.DRAWER_ITEM_HOME,
                    MainActivity.DRAWER_ITEM_TIMETABLE,
                    MainActivity.DRAWER_ITEM_AGENDA,
                    MainActivity.DRAWER_ITEM_GRADES,
                    MainActivity.DRAWER_ITEM_MESSAGES,
                    MainActivity.DRAWER_ITEM_HOMEWORK,
                    MainActivity.DRAWER_ITEM_SETTINGS
            )
            sync.enabled = true
            sync.interval = 1*HOUR.toInt()
            sync.notifyAboutUpdates = true
            sync.onlyWifi = false
            sync.quietHoursStart = 0
            sync.quietHoursEnd = 0
            sync.quietDuringLessons = false
            sync.tokenApp = null
            sync.tokenMobidziennik = null
            sync.tokenMobidziennikList = listOf()
            sync.tokenLibrus = null
            sync.tokenLibrusList = listOf()
            sync.tokenVulcan = null
            sync.tokenVulcanList = listOf()
            timetable.bellSyncMultiplier = 0
            timetable.bellSyncDiff = null
            timetable.countInSeconds = false
            grades.orderBy = ORDER_BY_DATE_DESC

            dataVersion = 2
        }

        if (dataVersion < 10) {
            ui.openDrawerOnBackPressed = false
            ui.homeCards = listOf()
            ui.snowfall = false
            ui.bottomSheetOpened = false
            sync.dontShowAppManagerDialog = false

            dataVersion = 10
        }
    }}
}
