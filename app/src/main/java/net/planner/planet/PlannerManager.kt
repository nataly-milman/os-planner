package net.planner.planet

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.*

class PlannerManager(syncGoogleCalendar: Boolean, activity: Activity?, startingFrom: Long?) : ActivityCompat.OnRequestPermissionsResultCallback  {
    private val TAG = "PlannerManager"

    private val calendar: PlannerCalendar
    private var shouldSync: Boolean
    private var communicator: GoogleCalenderCommunicator? = null
    private val callerActivity: Activity?
    private val calendarStartTime: Long = startingFrom ?: System.currentTimeMillis()

    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("H:mm MM/dd/yyyy")
    init {
        if (calendarStartTime < 0){
            throw IllegalArgumentException("Error setting up user calendar - invalid start datetime, cannot be before " +
                    "Thu Jan 01, 1970")
        }
        calendar = PlannerCalendar(calendarStartTime)
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
        val events = communicator?.getUserEvents(strongActivity, calendarStartTime)
        if (events != null) {
            // Will reach only if user already gave us permissions
            for (event in events) {
                calendar.insertEvent(event)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @JvmOverloads
    fun addEvent(title: String = "", startTime: Long, endTime: Long,
                 isAllDay : Boolean = false, canBeScheduledOver : Boolean = true,
                 description: String = "", location: String = "", tag: String = "NoTag") {
        if (tag != "NoTag" && !calendar.containsTag(tag)){
            throw IllegalArgumentException(
                "This tag doesn't exist"
            )
        }

        val event = createEvent(title, startTime, endTime, isAllDay, canBeScheduledOver, description, location, tag)

        calendar.insertEvent(event)
        // If this is a synced calendar, should be added to the users google calendar
        if (this.shouldSync) {
            Log.d(TAG, "addEvent: Adding created event to google calendar, default calendar")
            val id = communicator?.insertEvent(callerActivity?.contentResolver, event)?.let {
                event.setEventId(it)
            }
        }
    }

    private fun createEvent(title: String = "", startTime: Long, endTime: Long,
                            isAllDay : Boolean = false, canBeScheduledOver : Boolean = true,
                            description: String = "", location: String = "", tag: String = "NoTag") : PlannerEvent {

        val event = PlannerEvent(title, startTime, endTime)
        event.setLocation(location)
        event.tagName = tag
        event.isExclusiveForItsTimeSlot = canBeScheduledOver
        event.setAllDay(isAllDay)

        event.setDescription(description)

        if (isAllDay){
            var date = (Calendar.getInstance().apply { timeInMillis = startTime })
            val newStartDate = SimpleDateFormat("MM/dd/yyyy").format(date.time)
            event.startTime = formatter.parse("0:0 $newStartDate").time

            date = (Calendar.getInstance().apply {
                timeInMillis = startTime
            })
            val newEndDate = SimpleDateFormat("MM/dd/yyyy").format(date.time)
            event.endTime = formatter.parse("23:59 $newEndDate").time
        }

        return event
    }

    @JvmOverloads
    fun createTask(title: String, deadlineTimeMillis: Long, durationInMinutes: Int, tag: String = "NoTag",
                priority: Int = 5, location: String = ""): PlannerTask {
        if (tag != "NoTag" && !calendar.containsTag(tag)) {
            throw IllegalArgumentException(
                "This tag doesn't exist"
            )
        }
        val task = PlannerTask(title, deadlineTimeMillis, durationInMinutes)
        task.setPriority(priority)
        task.setLocation(location)
        task.tagName = tag
        return task
    }

    @JvmOverloads
    fun addTask(title: String, deadlineTimeMillis: Long, durationInMinutes: Int, tag: String = "NoTag",
                priority: Int = 9, location: String = "") {
        val task = createTask(title, deadlineTimeMillis, durationInMinutes, tag, priority, location)
        val wasAdded = calendar.insertTask(task)

        if (wasAdded && this.shouldSync) {
            Log.d(TAG, "addEvent: Adding created event to google calendar, default calendar")
            // Adding task to googleCalendar as Event - Currently all the time requested at once!
            val event = createEvent(title, deadlineTimeMillis - (durationInMinutes * 1000 * 60), deadlineTimeMillis, location = location, tag = tag)
            val id = communicator?.insertEvent(callerActivity?.contentResolver, event)?.let {
                event.setEventId(it)
            }
        }
    }

    fun addTasksList(tasks: List<PlannerTask>) {
        PlannerSolver.addTasks(tasks, calendar)
    }

    @SuppressLint("SimpleDateFormat")
    private fun turnTimesIntoDates(timeIntervals: List<Pair<Pair<Int, Int>, Pair<Int, Int>>>?): List<Pair<Long, Long>> {
        val dateIntervals = mutableListOf<Pair<Long, Long>>()
        val startDateD = (Calendar.getInstance().apply { timeInMillis = calendarStartTime })

        if (timeIntervals != null) {
            for (pair in timeIntervals) {
                val startHour = pair.first.first.toString()
                val startMin = pair.first.second.toString()
                val endHour = pair.second.first.toString()
                val endMin = pair.second.second.toString()
                for (i in 0..30) {
                    val startDate = SimpleDateFormat("MM/dd/yyyy").format(startDateD.time)
                    val start = formatter.parse("$startHour:$startMin $startDate").time
                    val end = formatter.parse("$endHour:$endMin $startDate").time
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
        if (calendar.containsTag(title)){
            calendar.removeTag(title)
        }
        calendar.addTag(tag)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "Permissions granted, calling communicator to add events")
        addEventsToCalendar()
    }

}