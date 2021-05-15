package net.planner.planet

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract

class GoogleCalenderCommunicator {

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private val CALENDAR_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Calendars._ID,                     // 0
        CalendarContract.Calendars.ACCOUNT_NAME,            // 1
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
        CalendarContract.Calendars.OWNER_ACCOUNT            // 3
    )

    private val EVENT_INSTANCE_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Instances.CALENDAR_ID, // 0
        CalendarContract.Instances.BEGIN, // 1
        CalendarContract.Instances.END // 2
    )

    companion object {
        // The indices for Calender projection array
        private const val PROJECTION_ID_INDEX: Int = 0
        private const val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
        private const val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
        private const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3

        // The indices for Event projection array
        // @TODO not sure about the numbers here, need to check what we get
        const val PROJECTION_EVENT_ID_INDEX: Int = 0
        const val PROJECTION_EVENT_BEGIN_INDEX: Int = 1
        const val PROJECTION_EVENT_END_INDEX: Int = 2
    }

    private val calenderIds = ArrayList<Long>()

    fun findAccountCalendars(accountUser: String, accountType: String, accountOwner: String, caller: Activity) {
        // Run query
        val contentResolver = caller.contentResolver
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val selection: String = "((${CalendarContract.Calendars.ACCOUNT_NAME} = ?) AND (" +
                "${CalendarContract.Calendars.ACCOUNT_TYPE} = ?) AND (" +
                "${CalendarContract.Calendars.OWNER_ACCOUNT} = ?))"
        val selectionArgs: Array<String> = arrayOf(accountUser, accountType, accountOwner)
        val cur: Cursor? = contentResolver.query(uri, CALENDAR_PROJECTION, selection, selectionArgs, null)

        while (cur?.moveToNext() == true) {
            // Get the field values
            val calenderID: Long = cur.getLong(PROJECTION_ID_INDEX)
            val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
            val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
            val ownerName: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
            // TODO store calenders information - see if there's more then one
            calenderIds.add(calenderID)
        }
    }


    // @TODO Maybe send Event object here then extract the needed values
    private fun insertEvent(contentResolver: ContentResolver, startMillis: Long, endMillis: Long, eventTitle: String, calenderId: Long, timezone: String? = null, eventDescription: String? = null ) : Long {
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, eventTitle)
            put(CalendarContract.Events.DESCRIPTION, eventDescription?: "")
            put(CalendarContract.Events.CALENDAR_ID, calenderId)
            put(CalendarContract.Events.EVENT_TIMEZONE, timezone ?: "America/Los_Angeles")
        }
        val uri: Uri? = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        // @TODO find out how to get confirmation since this is async probably relates to https://developer.android.com/reference/android/content/AsyncQueryHandler

        // get the event ID that is the last element in the Uri
        val eventID: Long = uri?.let {
            it.lastPathSegment?.toLong()
        } ?: 0
        return eventID
    }


    fun getCalendarEvents(contentResolver: ContentResolver, calenderId: Long, startMillis: Long, endMillis: Long) {

        // @TODO maybe use DTSTART instead
        val selection: String = "${CalendarContract.Instances.CALENDAR_ID} = ?"

        val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)

        val selectionArgs: Array<String> = arrayOf(calenderId.toString())

        val cur: Cursor? = contentResolver.query(builder.build(), EVENT_INSTANCE_PROJECTION, selection, selectionArgs, null

        // @TODO go over results in while and extract the needed values for events

        )

    }

    // @TODO add permission check like in: https://github.com/DilipSinghPanwar/AddEventGoogleCalender/blob/master/app/src/main/java/com/androidevs/CalendarHelper.java

}