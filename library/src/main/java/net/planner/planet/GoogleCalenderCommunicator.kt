package net.planner.planet

import android.Manifest
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
import kotlin.collections.ArrayList


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
        CalendarContract.Instances.EVENT_ID, // 0
        CalendarContract.Instances.BEGIN, // 1
        CalendarContract.Instances.END // 2
    )

    private val TAG = "GoogleCalenderCommunicator"

    companion object {

        const val PERMISSION_REQUEST_CODE = 99

        // The indices for Calender projection array
        private const val PROJECTION_ID_INDEX: Int = 0
        private const val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
        private const val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
        private const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3

        // The indices for Event projection array
        const val PROJECTION_EVENT_ID_INDEX: Int = 0
        const val PROJECTION_EVENT_BEGIN_INDEX: Int = 1
        const val PROJECTION_EVENT_END_INDEX: Int = 2
    }

    private val calenderIds = ArrayList<Long>()

    fun findAccountCalendars(contentResolver: ContentResolver):  ArrayList<Long> {

        // Run query to get all calendars in device
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val cur: Cursor? = contentResolver.query(uri, CALENDAR_PROJECTION, null, null, null)

        // Get calender Id's
        while (cur?.moveToNext() == true) {
            val calenderID: Long = cur.getLong(PROJECTION_ID_INDEX)
            val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
            val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
            val ownerName: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
            calenderIds.add(calenderID)
            Log.d(TAG, "findAccountCalendars: Found calender with name $displayName and id $calenderID of owner $ownerName ")
        }

        return calenderIds
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

        val cur: Cursor? = contentResolver.query(builder.build(), EVENT_INSTANCE_PROJECTION, selection, selectionArgs, null)

        while (cur?.moveToNext() == true) {
            // Get the field values
            val eventID: Long = cur.getLong(PROJECTION_EVENT_ID_INDEX)
            val eventStart: String = cur.getString(PROJECTION_EVENT_BEGIN_INDEX)
            val eventEnd: String = cur.getString(PROJECTION_EVENT_END_INDEX)

            val startDate = Calendar.getInstance().apply {
                timeInMillis = eventStart.toLong()
            }

            val endDate = Calendar.getInstance().apply {
                timeInMillis = eventEnd.toLong()
            }
            val formatter = SimpleDateFormat("MM/dd/yyyy")
            Log.d(TAG, "getCalendarEvents: found event with id $eventID, starts ${formatter.format(startDate.time)} ends ${formatter.format(endDate.time)} from calendar $calenderId ")
        }
    }


    fun getUserEvents(caller:Activity, startMillis: Long? = null, endMillis: Long? = null) {
        if (!haveCalendarReadWritePermissions(caller)) {
            requestCalendarReadWritePermission(caller)
            Log.d(TAG, "Needed permissions - returning")
            return
        }

        val contentResolver = caller.contentResolver
        findAccountCalendars(contentResolver)

        val strongStartMillis = startMillis ?: 0L // @TODO change to Month before
        val strongEndMillis = endMillis ?: System.currentTimeMillis()

        if (calenderIds.isNotEmpty()) {
            val firstId = calenderIds[0]
            val nowMillis = System.currentTimeMillis()
            getCalendarEvents(contentResolver, firstId, 0L, nowMillis)
        } else {
            Log.e(TAG, "getUserEvents: No calendar Ids")
        }

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