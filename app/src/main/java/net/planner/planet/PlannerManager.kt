package net.planner.planet

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.*

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

    @JvmOverloads
    fun addEvent(startTime: Long, endTime: Long, title: String = "",
                 isAllDay : Boolean = false, canBeScheduledOver : Boolean = true,
                 description: String = "", location: String = "", tag: String = "NoTag") {
        // todo if tag is not NoTag and not in the calendar, return exception
        val event = PlannerEvent(title,  startTime, endTime)
        event.setLocation(location)
        event.setTag(PlannerTag(tag))
        event.setExclusiveForItsTimeSlot(canBeScheduledOver)
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

    @JvmOverloads
    fun addTask(deadlineTimeMillis: Long, durationMillis: Long, title: String,
                priority: Int = 9, location: String = "", tag: String = "NoTag") {
        // todo if tag is not NoTag and not in the calendar, return exception
        val task = PlannerTask(title, deadlineTimeMillis, durationMillis)
        task.setPriority(priority)
        task.setLocation(location)
        task.setTag(PlannerTag(tag))
        calendar.insertTask(task)
        // @TODO add actions to calculate task subtask events
    }

    @SuppressLint("SimpleDateFormat")
    private fun turnTimesIntoDates(timeIntervals: List<Pair<Pair<Int, Int>, Pair<Int, Int>>>?): List<Pair<Long, Long>> {
        val dateIntervals = mutableListOf<Pair<Long, Long>>()
        val formatter = SimpleDateFormat("h:m MM/dd/yyyy")
        val startDateD = (Calendar.getInstance().apply {
            timeInMillis = calendar.startTime
        })

        if (timeIntervals != null) {
            for (pair in timeIntervals) {
                val startHour = pair.first.first.toString()
                val startMin = pair.first.second.toString()
                val endHour = pair.second.first.toString()
                val endMin = pair.second.second.toString()
                for (i in 0..30) {
                    val startDate = SimpleDateFormat("MM/dd/yyyy").format(startDateD.time)
                    val start = formatter.parse(startHour + ":" + startMin + " " + startDate).time
                    val end = formatter.parse(endHour + ":" + endMin + " " + startDate).time
                    dateIntervals.add(Pair(start, end))
                    startDateD.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }
        return dateIntervals
    }

    fun addOrEditTag(title: String, forbiddenTimeIntervals: List<Pair<Pair<Int, Int>, Pair<Int, Int>>>? = null,
               preferredTimeIntervals: List<Pair<Pair<Int, Int>, Pair<Int, Int>>>? = null, priority: Int = 5){
        val tag = PlannerTag(title)
        tag.priority = priority
        for (pair in turnTimesIntoDates(forbiddenTimeIntervals)){
            tag.addForbiddenTimeInterval(pair.first, pair.second)
        }

        for (pair in turnTimesIntoDates(preferredTimeIntervals)){
            tag.addPreferredTimeInterval(pair.first, pair.second)
        }
        // @TODO add to calendar - if it's in, rewrite if not - add

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "Permissions granted, calling communicator to add events")
        addEventsToCalendar()
    }

}