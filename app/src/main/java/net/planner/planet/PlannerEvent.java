package net.planner.planet;

import android.icu.util.DateInterval;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


public class PlannerEvent extends PlannerObject {
    private static final String TAG = "PlannerEvent";
    private long startTime;
    private long endTime;
    private long eventId;
    private boolean isAllDay;

    // constructors
    public PlannerEvent(String title, long startTime, long endTime) {
        super(title);
        if (endTime < startTime) {
            Log.e(TAG,"Illegal time interval: Event cannot end before it starts");
            return;
        }
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventId = -1L;
        this.isAllDay = false; // @TODO check duration?
    }
    // validity check
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
    public long getStartTime() {
        return startTime;
    }

    public boolean setStartTime(long startTime) {
        if (startTime < 0) {
            Log.e(TAG,"Illegal start time: Time cannot be negative");
            return false;
        }
        long duration = this.endTime - this.startTime;
        this.startTime = startTime;
        this.endTime = startTime + duration;
        return true;
    }

    public long getEndTime() {
        return endTime;
    }

    public boolean setEndTime(long endTime) {
        if (this.startTime > endTime) {
            Log.e(TAG,"Illegal end time: Event cannot end before it starts");
            return false;
        }
        this.endTime = endTime;
        return true;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public void setAllDay(boolean isAllDay) {
        this.isAllDay = isAllDay;
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
        return getStartTime() == that.getStartTime() && getEndTime() == that.getEndTime();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getStartTime(), getEndTime());
    }
}
