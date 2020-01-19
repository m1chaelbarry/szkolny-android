/*
 * Copyright (c) Kuba Szczodrzyński 2020-1-19.
 */

package pl.szczodrzynski.edziennik.config

import pl.szczodrzynski.edziennik.config.utils.get
import pl.szczodrzynski.edziennik.config.utils.set
import pl.szczodrzynski.edziennik.data.db.entity.Profile.Companion.AGENDA_DEFAULT

class ProfileConfigUI(private val config: ProfileConfig) {
    private var mAgendaViewType: Int? = null
    var agendaViewType: Int
        get() { mAgendaViewType = mAgendaViewType ?: config.values.get("agendaViewType", 0); return mAgendaViewType ?: AGENDA_DEFAULT }
        set(value) { config.set("agendaViewType", value); mAgendaViewType = value }
}