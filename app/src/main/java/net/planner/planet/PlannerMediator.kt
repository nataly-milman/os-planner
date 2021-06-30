package net.planner.planet

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.*

class PlannerMediator(syncGoogleCalendar: Boolean, activity: Activity?, startingFrom: Long?) :
    ActivityCompat.OnRequestPermissionsResultCallback {
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
            Log.e(TAG,"Error setting up user calendar - invalid start datetime, cannot be before " +
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
                Log.e(
                    TAG,
                    "Error getting user connected events - Cannot get google calendar events without an activity "
                )
            }
        }
    }

    /** Add events from the Google Calendar to PlannerCalendar**/
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

    /** Add PlannerEvent created from the given parameters to the calendar **/
    @SuppressLint("SimpleDateFormat")
    @JvmOverloads
    fun addEvent(title: String = "", startTime: Long, endTime: Long,
                 isAllDay : Boolean = false, canBeScheduledOver : Boolean = true,
                 description: String = "", location: String = "", tagName: String = "NoTag",
                 reminder : Int = -1): PlannerEvent? {
        if (!PlannerEvent.isValid(reminder, startTime, endTime)){
            return null
        }
        if (tagName != "NoTag" && !calendar.containsTag(tagName)) {
            calendar.addTag(PlannerTag(tagName))
        }

        val event = createEvent(
            title,
            startTime,
            endTime,
            isAllDay,
            canBeScheduledOver,
            description,
            location,
            tagName
        )

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

    /** Create a PlannerEvent object out of its parameters **/
    private fun createEvent(title: String = "", startTime: Long, endTime: Long,
                            isAllDay : Boolean = false, canBeScheduledOver : Boolean = true,
                            description: String = "", location: String = "", tagName: String = "NoTag",
                            reminder : Int = -1) : PlannerEvent {
        // private function, the validity checks are performed in addEvent
        val event = PlannerEvent(title, startTime, endTime)
        event.setLocation(location)
        event.tagName = tagName
        event.isExclusiveForItsTimeSlot = canBeScheduledOver
        event.setAllDay(isAllDay)
        event.setReminder(reminder)
        event.setDescription(description)
        return event
    }

    /** Remove a PlannerEvent object from the calendar **/
    fun removeEvent(event: PlannerEvent) {
        calendar.removeEvent(event)
    }

    /** Create a PlannerTask object out of its parameters **/
    @JvmOverloads
    fun createTask(
        title: String, deadlineTimeMillis: Long, durationInMinutes: Int, tagName: String = "NoTag",
        priority: Int = 5, location: String = "", maxSessionTimeInMinutes: Int = 60,
        maxDivisionsNumber: Int = 1, reminder: Int = -1
    ): PlannerTask {
        if (tagName != "NoTag" && !calendar.containsTag(tagName)) {
            calendar.addTag(PlannerTag(tagName))
        }
        val task = PlannerTask(title, deadlineTimeMillis, durationInMinutes)
        task.setPriority(priority)
        task.setLocation(location)
        task.tagName = tagName
        task.maxSessionTimeInMinutes = maxSessionTimeInMinutes
        task.maxDivisionsNumber = maxDivisionsNumber
        task.reminder = reminder
        return task
    }

    /** Add the instances of the PlannerTask created by its parameters to the calendar **/
    @JvmOverloads
    fun addTask(title: String, deadlineTimeMillis: Long, durationInMinutes: Int, tagName: String = "NoTag",
                priority: Int = 9, location: String = "", maxSessionTimeInMinutes: Int = 60,
                maxDivisionsNumber: Int = 1, reminder: Int = -1): MutableList<PlannerEvent>? {
        if (!PlannerTask.isValid(reminder, priority, deadlineTimeMillis, durationInMinutes,
            maxSessionTimeInMinutes,maxDivisionsNumber)){
            return null
        }

        val task = createTask(title, deadlineTimeMillis, durationInMinutes, tagName, priority, location,
            maxSessionTimeInMinutes, maxDivisionsNumber, reminder)
        val insertedEvents = PlannerSolver.addTask(task, calendar)
        if (insertedEvents.isNotEmpty() && this.shouldSync) {
            Log.d(TAG, "addEvent: Adding created event to google calendar, default calendar")
            // Adding task to googleCalendar as Event - Currently all the time requested at once!
            val event = insertedEvents[0]
            event.setLocation(location)
            event.tagName = tagName

            val id = communicator?.insertEvent(callerActivity?.contentResolver, event)?.let {
                event.setEventId(it)
            }
        }
        return insertedEvents
    }

    /** Add the list of PlannerTask to the calendar **/
    fun addTasksList(tasks: List<PlannerTask>): MutableList<MutableList<PlannerEvent>>? {
        // returns list of the inserted tasks (may not include all tasks if failed on any of them
        return PlannerSolver.addTasks(tasks, calendar)
    }

    /** Turn given time intervals to a list of date-specific time intervals for this calendar  **/
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

    /** Add or rewrite tag with given name **/
    fun addOrRewriteTag(
        title: String,
        forbiddenTimeIntervals: List<Pair<Pair<Int, Int>, Pair<Int, Int>>>? = null,
        preferredTimeIntervals: List<Pair<Pair<Int, Int>, Pair<Int, Int>>>? = null,
        priority: Int = 5
    ): PlannerTag {
        val tag = PlannerTag(title)
        tag.priority = priority
        for (pair in turnTimesIntoDates(forbiddenTimeIntervals)) {
            tag.addForbiddenTimeInterval(pair.first, pair.second)
        }

        for (pair in turnTimesIntoDates(preferredTimeIntervals)) {
            tag.addPreferredTimeInterval(pair.first, pair.second)
        }
        if (calendar.containsTag(title)) {
            calendar.removeTag(title)
        }
        calendar.addTag(tag)
        return tag
    }

    /** Get list of tag names in the calendar **/
    fun getTagNameList(): MutableList<String>? {
        return calendar.tagNames;
    }

    /** Remove the tag with given name from the calendar **/
    fun removeTag(title: String) {
        if (calendar.containsTag(title)) {
            calendar.removeTag(title)
        }
    }

    /** Rename the tag with given name in the calendar **/
    fun renameTag(oldTitle: String, newTitle: String): PlannerTag? {
        if (!calendar.containsTag(oldTitle)) {
            return null
        }

        val tag = calendar.getTag(oldTitle)
        calendar.removeTag(oldTitle)
        tag.tagName = newTitle
        calendar.addTag(tag)
        return tag
    }

    /** Add a time interval to the list of tag's time intervals **/
    fun addToTag(
        title: String, forbiddenTimeInterval: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null,
        preferredTimeInterval: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null
    ): PlannerTag? {
        if (!calendar.containsTag(title)) {
            return null
        }
        val tag = calendar.getTag(title)
        for (pair in turnTimesIntoDates(listOfNotNull(forbiddenTimeInterval))) {
            tag.addForbiddenTimeInterval(pair.first, pair.second)
        }

        for (pair in turnTimesIntoDates(listOfNotNull(preferredTimeInterval))) {
            tag.addPreferredTimeInterval(pair.first, pair.second)
        }
        return tag
    }

    /** results of Google Calendar permissions request **/
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "Permissions granted, calling communicator to add events")
        addEventsToCalendar()
    }

}