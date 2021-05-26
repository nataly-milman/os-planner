package net.planner.planet

import android.app.Activity

class PlannerManager(syncGoogleCalendar: Boolean) {
    private val calendar: PlannerCalendar
    private val shouldSync: Boolean

    init {
        calendar = PlannerCalendar()
        shouldSync = syncGoogleCalendar
        //
    }

}