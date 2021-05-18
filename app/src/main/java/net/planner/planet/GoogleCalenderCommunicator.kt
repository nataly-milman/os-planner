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
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class GoogleCalenderCommunicator : ActivityCompat.OnRequestPermissionsResultCallback {

    private val TAG = "GoogleCalenderCommunicator"
    private val ONE_MONTH_MILLIS = TimeUnit.DAYS.toMillis(30)

    private val calenderIds = ArrayList<Long>()


    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private val CALENDAR_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Calendars._ID,                     // 0
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 1
        CalendarContract.Calendars.OWNER_ACCOUNT            // 2
    )

    private val EVENT_INSTANCE_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Instances.EVENT_ID, // 0
        CalendarContract.Instances.BEGIN, // 1
        CalendarContract.Instances.END // 2
    )

    companion object {

        const val PERMISSION_REQUEST_CODE = 99

        // The indices for Calender projection array
        private const val PROJECTION_ID_INDEX: Int = 0
        private const val PROJECTION_DISPLAY_NAME_INDEX: Int = 1
        private const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 2

        // The indices for Event projection array
        const val PROJECTION_EVENT_ID_INDEX: Int = 0
        const val PROJECTION_EVENT_BEGIN_INDEX: Int = 1
        const val PROJECTION_EVENT_END_INDEX: Int = 2
    }


    fun findAccountCalendars(contentResolver: ContentResolver):  ArrayList<Long> {

        // Run query to get all calendars in device
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val cur: Cursor? = contentResolver.query(uri, CALENDAR_PROJECTION, null, null, null)

        // Get calender Id's
        while (cur?.moveToNext() == true) {
            val calenderID: Long = cur.getLong(PROJECTION_ID_INDEX)
            val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
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
            put(CalendarContract.Events.EVENT_TIMEZONE, timezone ?: TimeZone.getDefault().displayName)
        }
        val uri: Uri? = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        // @TODO find out how to get confirmation since this is async probably relates to https://developer.android.com/reference/android/content/AsyncQueryHandler

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

    // @TODO Do not return anything yet because I want to have the Event class first
    private fun getCalendarEvents(contentResolver: ContentResolver, calenderId: Long, startMillis: Long, endMillis: Long) {

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

        val strongStartMillis = startMillis ?: System.currentTimeMillis() - ONE_MONTH_MILLIS
        val strongEndMillis = endMillis ?: System.currentTimeMillis()

        if (calenderIds.isNotEmpty()) {
            for (id in calenderIds ) {
                getCalendarEvents(contentResolver, id, strongStartMillis, strongEndMillis)
            }
        } else {
            Log.e(TAG, "getUserEvents: No calendar Ids")
        }

    }


    fun requestCalendarReadWritePermission(caller: Activity) {
        val permissionList: MutableList<String> = ArrayList()

        if (ContextCompat.checkSelfPermission(caller, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_CALENDAR)
        }

        if (ContextCompat.checkSelfPermission(caller, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
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

    // @TODO Either we implement this here was some kind of object that remembers the call to the function, or the caller needs to implements this to remember to call again
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "Permissions granted")
    }
}