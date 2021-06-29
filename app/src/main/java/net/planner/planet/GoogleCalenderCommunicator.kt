package net.planner.planet

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class GoogleCalenderCommunicator {

    private val TAG = "GoogleCalenderCommunicator"
    private val ONE_MONTH_MILLIS = TimeUnit.DAYS.toMillis(30)

    private val calenderIds = mutableSetOf<Long>()
    private var mainCalendarID =
        1L // Calendar to insert new events into by default. Selected as a calendar with the ending gmail.com or  default 1


    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private val CALENDAR_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.OWNER_ACCOUNT,
        CalendarContract.Calendars.IS_PRIMARY
    )

    private val EVENT_INSTANCE_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Instances.EVENT_ID,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.END
    )


    private val EVENT_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Events._ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DESCRIPTION,
        CalendarContract.Events.ALL_DAY,
        CalendarContract.Events.AVAILABILITY,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.DISPLAY_COLOR
    )

    companion object {

        const val PERMISSION_REQUEST_CODE = 99

        // The indices for Calender projection array
        private const val PROJECTION_ID_INDEX: Int = 0
        private const val PROJECTION_DISPLAY_NAME_INDEX: Int = 1
        private const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 2
        private const val PROJECTION_IS_MAIN_ACCOUNT_INDEX: Int = 3

        // The indices for Event Instances projection array
        const val PROJECTION_EVENT_INSTANCE_ID_INDEX: Int = 0
        const val PROJECTION_EVENT_INSTANCE_BEGIN_INDEX: Int = 1
        const val PROJECTION_EVENT_INSTANCE_END_INDEX: Int = 2

        // The indices for Event projection array
        const val PROJECTION_EVENT_ID_INDEX: Int = 0
        const val PROJECTION_EVENT_TITLE_INDEX: Int = 1
        const val PROJECTION_DESCRIPTION_INDEX: Int = 2
        const val PROJECTION_ALL_DAY_INDEX: Int = 3
        const val PROJECTION_AVAILABILITY_INDEX: Int = 4
        const val PROJECTION_EVENT_LOCATION_INDEX: Int = 5
        const val PROJECTION_DISPLAY_COLOR_INDEX: Int = 6
    }


    fun findAccountCalendars(contentResolver: ContentResolver): MutableCollection<Long> {

        // Run query to get all calendars in device
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val cur: Cursor? = contentResolver.query(uri, CALENDAR_PROJECTION, null, null, null)

        // Get calender Id's
        while (cur?.moveToNext() == true) {
            val calenderID: Long = cur.getLong(PROJECTION_ID_INDEX)
            val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
            val ownerName: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
            val primary: Int = cur.getInt(PROJECTION_IS_MAIN_ACCOUNT_INDEX)

            if (displayName.contains("gmail") && primary == 1) {
                Log.d(TAG, "findAccountCalendars: Primary and Gmail!")
                mainCalendarID = calenderID
            }

            calenderIds.add(calenderID)
            Log.d(
                TAG,
                "findAccountCalendars: Found calender with name $displayName and id $calenderID of owner $ownerName "
            )
        }
        cur?.close()

        return calenderIds
    }


    fun insertEvent(
        contentResolver: ContentResolver?, insertedEvent: PlannerEvent,
        calenderId: Long = mainCalendarID, timezone: String? = null
    ): Long {
        val startMillis = insertedEvent.startTime
        val endMillis = insertedEvent.endTime
        val eventTitle = insertedEvent.title
        val eventDescription = insertedEvent.description

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, eventTitle)
            put(CalendarContract.Events.DESCRIPTION, eventDescription ?: "")
            put(CalendarContract.Events.CALENDAR_ID, calenderId)
            put(
                CalendarContract.Events.EVENT_TIMEZONE,
                timezone ?: TimeZone.getDefault().displayName
            )
        }
        val uri: Uri? = contentResolver?.insert(CalendarContract.Events.CONTENT_URI, values)

        // get the event ID that is the last element in the Uri
        val eventID: Long = uri?.let {
            it.lastPathSegment?.toLong()
        } ?: 0
        return eventID
    }

//    fun insertTrialEvent(contentResolver: ContentResolver) {
//        val startMillis = System.currentTimeMillis()
//        val endMillis = startMillis
//        val eventTitle = "trying library"
//        val calendarId = 3L
//        val timeZone = TimeZone.getDefault().displayName
//        Log.d(TAG, "Time zone is $timeZone ")
//        insertEvent(contentResolver, startMillis, endMillis, eventTitle, calendarId, timeZone)
//    }


    @SuppressLint("SimpleDateFormat")
    private fun getCalendarEvents(
        contentResolver: ContentResolver,
        calenderId: Long,
        startMillis: Long,
        endMillis: Long
    ): MutableCollection<PlannerEvent> {
        // Getting events process:
        // 1. Get the event information from the Events table.
        // 2. For each event, check if it's an all day event.
        // 3. For each event, add all the instances from the Instances table.
        // 4. For each instance, convert time to current timezone if needed.

        val events = mutableListOf<PlannerEvent>()

        // Run query to get all event instances in the calendar
        val instancesSelection: String = "${CalendarContract.Instances.CALENDAR_ID} = ?"

        val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)

        val selectionArgs: Array<String> = arrayOf(calenderId.toString())

        val instancesCur: Cursor? = contentResolver.query(
            builder.build(),
            EVENT_INSTANCE_PROJECTION,
            instancesSelection,
            selectionArgs,
            null
        )

        while (instancesCur?.moveToNext() == true) {
            // Get the instance values
            val eventID: Long = instancesCur.getLong(PROJECTION_EVENT_INSTANCE_ID_INDEX)
            val eventStart: String = instancesCur.getString(PROJECTION_EVENT_INSTANCE_BEGIN_INDEX)
            val eventEnd: String = instancesCur.getString(PROJECTION_EVENT_INSTANCE_END_INDEX)

            // Note - The Calendar object is created with default Locale and Timezone. It expects time in milliseconds in UTC from the epoch and converts it to the default timezone
            val startDate = Calendar.getInstance().apply {
                timeInMillis = eventStart.toLong()
            }

            val endDate = Calendar.getInstance().apply {
                timeInMillis = eventEnd.toLong()
            }
            val formatter = SimpleDateFormat("MM/dd/yyyy")
            Log.d(
                TAG,
                "getCalendarEvents: found event with id $eventID, starts ${
                    formatter.format(startDate.time)
                } ends ${formatter.format(endDate.time)} from calendar $calenderId "
            )

            // Get the details of this event from the Events table
            val eventsUri: Uri = CalendarContract.Events.CONTENT_URI
            val eventsSelection: String = "${CalendarContract.Events._ID} = ?"
            val eventSelectionArgs: Array<String> = arrayOf(eventID.toString())
            val eventsCur: Cursor? = contentResolver.query(
                eventsUri,
                EVENT_PROJECTION,
                eventsSelection,
                eventSelectionArgs,
                null
            )

            while (eventsCur?.moveToNext() == true) {
                val id: Long = eventsCur.getLong(PROJECTION_EVENT_ID_INDEX)
                val title: String = eventsCur.getString(PROJECTION_EVENT_TITLE_INDEX)
                val description: String = eventsCur.getString(PROJECTION_DESCRIPTION_INDEX)
                val allDay: Int = eventsCur.getInt(PROJECTION_ALL_DAY_INDEX)
                val availability: Int = eventsCur.getInt(PROJECTION_AVAILABILITY_INDEX)
                val location: String = eventsCur.getString(PROJECTION_EVENT_LOCATION_INDEX)
                val color: Int = eventsCur.getInt(PROJECTION_DISPLAY_COLOR_INDEX)

                Log.d(
                    TAG,
                    "getCalendarEvents: found event with id $id, titled ${title} with description: ${description} is all day? $allDay can be scheduled over? $availability, in location $location  with color: $color from calendar: $calenderId "
                )
                val event = PlannerEvent(title, startDate.timeInMillis, endDate.timeInMillis)
                event.setLocation(location)
                event.tagName = color.toString()
                event.exclusiveForItsTimeSlot = CalendarContract.Events.AVAILABILITY_BUSY != 0
                event.setEventId(id)
                event.setAllDay(allDay == 1)
                event.setDescription(description)

                events.add(event)
            }
            eventsCur?.close()
        }
        instancesCur?.close()
        return events
    }


    fun getUserEvents(
        caller: Activity,
        startMillis: Long? = null,
        endMillis: Long? = null
    ): MutableCollection<PlannerEvent>? {
        if (!haveCalendarReadWritePermissions(caller)) {
            requestCalendarReadWritePermission(caller)
            Log.d(TAG, "Needed permissions - returning")
            return null
        }

        val contentResolver = caller.contentResolver
        findAccountCalendars(contentResolver)

        val allEvents = mutableListOf<PlannerEvent>()
        val strongStartMillis = startMillis ?: System.currentTimeMillis()
        val strongEndMillis = endMillis ?: strongStartMillis + ONE_MONTH_MILLIS

        if (calenderIds.isNotEmpty()) {
            for (id in calenderIds) {
                val events =
                    getCalendarEvents(contentResolver, id, strongStartMillis, strongEndMillis)
                allEvents.addAll(events)
            }
        } else {
            Log.e(TAG, "getUserEvents: No calendar Ids")
        }
        return allEvents

    }


    fun requestCalendarReadWritePermission(caller: Activity) {
        val permissionList: MutableList<String> = ArrayList()

        if (ContextCompat.checkSelfPermission(
                caller,
                Manifest.permission.WRITE_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(Manifest.permission.WRITE_CALENDAR)
        }

        if (ContextCompat.checkSelfPermission(
                caller,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(Manifest.permission.READ_CALENDAR)
        }

        if (permissionList.size > 0) {
            val permissionArray = arrayOfNulls<String>(permissionList.size)
            for (i in permissionList.indices) {
                permissionArray[i] = permissionList[i]
            }

            ActivityCompat.requestPermissions(
                caller,
                permissionArray,
                PERMISSION_REQUEST_CODE
            )

        }
    }


    fun haveCalendarReadWritePermissions(caller: Activity): Boolean {

        var permissionCheck = ContextCompat.checkSelfPermission(
            caller,
            Manifest.permission.READ_CALENDAR
        )

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            permissionCheck = ContextCompat.checkSelfPermission(
                caller,
                Manifest.permission.WRITE_CALENDAR
            )

            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }

        return false
    }

}