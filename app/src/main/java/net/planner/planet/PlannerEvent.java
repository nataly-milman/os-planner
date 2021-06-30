package net.planner.planet;

import android.util.Log;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


public class PlannerEvent extends PlannerObject {
    private static final String TAG = "PlannerEvent";
    private long startTime;
    private long endTime;
    private long eventId;
    private boolean isAllDay;
    private final PlannerTask parentTask;

    // constructors
    /** Create PlannerEvent object from its title and times **/
    public PlannerEvent(String title, long startTime, long endTime) {
        super(title);
        this.parentTask = null;
        if (endTime < startTime) {
            Log.e(TAG,"Illegal time interval: Event cannot end before it starts");
            return;
        }
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventId = -1L;
        this.isAllDay = false;
    }

    /** Create PlannerEvent object from the relevant Planner Task and times **/
    public PlannerEvent(PlannerTask task, long startTime, long endTime) {
        super(task.title);
        this.title = task.title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventId = -1L;
        this.isAllDay = false;
        this.parentTask = task;
    }

    // validity check
    /** Input parameters validity check **/
    public static boolean isValid(int reminder, long startTime, long endTime) {
        if (!PlannerObject.isValid(reminder)) {
            return false;
        }

        if (startTime < 0 || endTime < 0 ) {
            Log.e(TAG,"Validation error: Illegal time value");
            return false;
        }

        if (startTime > endTime) {
            Log.e(TAG,"Validation error: Event cannot end before it starts");
            return false;
        }

        return true;
    }

    // methods
    /** Get start time of the event in milliseconds **/
    public long getStartTime() {
        return startTime;
    }

    /** Set start time for the event in milliseconds, midnight only for all day events **/
    public boolean setStartTime(long startTime) {
        if (startTime < 0) {
            Log.e(TAG,"Illegal start time: Time cannot be negative");
            return false;
        }

        if (this.isAllDay && moveDeltaDaysTime(startTime,0, false) != endTime){
            Log.e(TAG,"Illegal start time: all day events should start at midnight");
            return false;
        }
        long duration = this.endTime - this.startTime;
        this.startTime = startTime;
        this.endTime = startTime + duration;
        return true;
    }

    /** Get end time of the event in milliseconds **/
    public long getEndTime() {
        return endTime;
    }

    /** Set end time for the event in milliseconds, midnight only for all day events **/
    public boolean setEndTime(long endTime) {
        if (this.startTime > endTime) {
            Log.e(TAG,"Illegal end time: Event cannot end before it starts");
            return false;
        }
        if (this.isAllDay && moveDeltaDaysTime(endTime,0, false) != endTime){
            Log.e(TAG,"Illegal end time: all day events should end at midnight");
            return false;
        }
        this.endTime = endTime;
        return true;
    }

    /** Set event id from Google Calendar **/
    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    /** Gets time in milliseconds and returns time of + delta days at 00:00 AM **/
    private long moveDeltaDaysTime(long timeInMillis, int delta, boolean force){
        long newTimeInMillis = timeInMillis;
        Calendar timeAsCal = Calendar.getInstance();
        timeAsCal.setTimeInMillis( timeInMillis );
        if (force || timeAsCal.get(Calendar.HOUR) != 0 || timeAsCal.get(Calendar.MINUTE) != 0) {
            timeAsCal.add(Calendar.DATE, delta);
            timeAsCal.set(Calendar.HOUR, 0);
            timeAsCal.set(Calendar.MINUTE, 0);
            newTimeInMillis = timeAsCal.getTimeInMillis();
        }
        return newTimeInMillis;
    }

    /** All day events are from 00:00AM of the first day until 00:00AM of the one after the last **/
    public void setAllDay(boolean isAllDay) {
        this.isAllDay = isAllDay;
        if (isAllDay) {
            startTime = moveDeltaDaysTime(startTime, 0, false);
            endTime = moveDeltaDaysTime(endTime, 1, false);
            if (startTime == endTime){
                endTime = moveDeltaDaysTime(endTime, 1, true);
            }
        }
    }

    /** Return PlannerTask object related to this event or null if there is none **/
    public final PlannerTask getParentTask() {
        return parentTask;
    }

    @NotNull
    @Override
    public String toString() {
        String stringRep = super.toString();
        stringRep +=
                "; Starts at " + new Date(this.startTime) + "; Ends at " + new Date(this.endTime);
        return stringRep + ".";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlannerEvent)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PlannerEvent that = (PlannerEvent) o;

        boolean doesParentTaskMatch = parentTask == that.parentTask || parentTask.equals(that.parentTask);
        return doesParentTaskMatch && getStartTime() == that.getStartTime() && getEndTime() == that.getEndTime();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getStartTime(), getEndTime());
    }
}
