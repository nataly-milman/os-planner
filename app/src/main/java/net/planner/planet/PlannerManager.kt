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
                 description: String = "", location: String = "", tagName: String = "NoTag"): PlannerEvent {
        if (tagName != "NoTag" && !calendar.containsTag(tagName)) {
            calendar.addTag(PlannerTag(tagName))
        }

        val event = createEvent(title, startTime, endTime, isAllDay, canBeScheduledOver, description, location, tagName)

        calendar.insertEvent(event)
        // If this is a synced calendar, should be added to the users google calendar
        if (this.shouldSync) {
            Log.d(TAG, "addEvent: Adding created event to google calendar, default calendar")
            val id = communicator?.insertEvent(callerActivity?.contentResolver, event)?.let {
                event.setEventId(it)
            }
        }
        return event
    }

    private fun createEvent(title: String = "", startTime: Long, endTime: Long,
                            isAllDay : Boolean = false, canBeScheduledOver : Boolean = true,
                            description: String = "", location: String = "", tagName: String = "NoTag") : PlannerEvent {
        // private function, the validity checks are performed in addEvent
        val event = PlannerEvent(title, startTime, endTime)
        event.setLocation(location)
        event.tagName = tagName
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
    fun createTask(title: String, deadlineTimeMillis: Long, durationInMinutes: Int, tagName: String = "NoTag",
                   priority: Int = 5, location: String = ""): PlannerTask {
        if (tagName != "NoTag" && !calendar.containsTag(tagName)) {
            calendar.addTag(PlannerTag(tagName))
        }
        val task = PlannerTask(title, deadlineTimeMillis, durationInMinutes)
        task.setPriority(priority)
        task.setLocation(location)
        task.tagName = tagName
        return task
    }

    @JvmOverloads
    fun addTask(title: String, deadlineTimeMillis: Long, durationInMinutes: Int, tagName: String = "NoTag",
                priority: Int = 9, location: String = ""): PlannerTask {
        val task = createTask(title, deadlineTimeMillis, durationInMinutes, tagName, priority, location)
        val insertedTask = PlannerSolver.addTask(task, calendar)

        if (insertedTask != null && this.shouldSync) {
            Log.d(TAG, "addEvent: Adding created event to google calendar, default calendar")
            // Adding task to googleCalendar as Event - Currently all the time requested at once!
            val event = createEvent(title, deadlineTimeMillis - (durationInMinutes * 1000 * 60), deadlineTimeMillis, location = location, tagName = tagName)
            val id = communicator?.insertEvent(callerActivity?.contentResolver, event)?.let {
                event.setEventId(it)
            }
        }
        return insertedTask
    }

    fun addTasksList(tasks: List<PlannerTask>): MutableList<PlannerTask>? {
        // returns list of the inserted tasks (may not include all tasks if failed on any of them
        return PlannerSolver.addTasks(tasks, calendar)
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

    fun addOrRewriteTag(title: String, forbiddenTimeIntervals: List<Pair<Pair<Int, Int>, Pair<Int, Int>>>? = null,
                        preferredTimeIntervals: List<Pair<Pair<Int, Int>, Pair<Int, Int>>>? = null, priority: Int = 5): PlannerTag {
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
        return tag
    }

    fun removeTag(title: String){
        if (calendar.containsTag(title)){
            calendar.removeTag(title)
        }
    }

    fun renameTag(oldTitle: String, newTitle: String): PlannerTag? {
        if (!calendar.containsTag(oldTitle)){ return null}

        val tag = calendar.getTag(oldTitle)
        calendar.removeTag(oldTitle)
        tag.tagName = newTitle
        calendar.addTag(tag)
        return tag
    }

    fun addToTag(title: String, forbiddenTimeInterval: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null,
                        preferredTimeInterval: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null): PlannerTag? {
        if (!calendar.containsTag(title)){ return null}
        val tag = calendar.getTag(title)
        for (pair in turnTimesIntoDates(listOfNotNull(forbiddenTimeInterval))){
            tag.addForbiddenTimeInterval(pair.first, pair.second)
        }

        for (pair in turnTimesIntoDates(listOfNotNull(preferredTimeInterval))){
            tag.addPreferredTimeInterval(pair.first, pair.second)
        }
        return tag
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "Permissions granted, calling communicator to add events")
        addEventsToCalendar()
    }

}