package net.planner.planet

import android.app.Activity
import android.util.Log
import androidx.core.app.ActivityCompat

class PlannerManager(syncGoogleCalendar: Boolean, activity: Activity?) : ActivityCompat.OnRequestPermissionsResultCallback  {
    private val TAG = "PlannerManager"

    private val calendar: PlannerCalendar
    private var shouldSync: Boolean
    private var communicator: GoogleCalenderCommunicator? = null
    private val callerActivity: Activity?

    init {
        calendar = PlannerCalendar()
        shouldSync = syncGoogleCalendar
        callerActivity = activity

        if (shouldSync) {
            communicator = GoogleCalenderCommunicator()
            // Add events from the user's google calendars
            if (activity != null) {
                if (communicator?.haveCalendarReadWritePermissions(activity) == true) {
                    addEventsToCalendar()
                } else {
                    communicator?.requestCalendarReadWritePermission(activity)
                }

            } else {
                Log.e(TAG, "Error getting user connected events - Cannot get google calendar events without an activity ")
            }
        }
    }

    private fun addEventsToCalendar() {
        val strongActivity: Activity = callerActivity ?: return
        val events = communicator?.getUserEvents(strongActivity)
        if (events != null) {
            // Will reach only if user already gave us permissions
            for (event in events) {
                calendar.insertEvent(event)
            }
        }
    }

    fun addEvent(startTime: Long, endTime: Long, title: String = "",
                 isAllDay : Boolean = false, canBeScheduledOver : Boolean = true,
                 description: String = "", location: String = "", tag: String = "NoTag") {

        val event = PlannerEvent(title,  startTime, endTime)
        event.setLocation(location)
        event.setTag(PlannerTag(tag))
        event.exclusiveForItsTimeSlot = canBeScheduledOver
        event.setAllDay(isAllDay)
        event.setDescription(description)
        calendar.insertEvent(event)

        // If this is a synced calendar, should be added to the users google calendar
        if (this.shouldSync) {
            Log.d(TAG, "addEvent: Adding created event to google calendar, default calendar")
            val id = communicator?.insertEvent(callerActivity?.contentResolver, event)?.let {
                event.setEventId(it)
            }
        }
    }

    fun addTask(deadlineTimeMillis: Long, durationMillis: Long, title: String,
                priority: Int = 9, location: String = "", tag: String = "NoTag") {

        val task = PlannerTask(title, deadlineTimeMillis, durationMillis)
        task.setPriority(priority)
        task.setLocation(location)
        task.setTag(PlannerTag(tag))
        calendar.addTask(task)
        // @TODO add actions to calculate task subtask events
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "Permissions granted, calling communicator to add events")
        addEventsToCalendar()
    }

}