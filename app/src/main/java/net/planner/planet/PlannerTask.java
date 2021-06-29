package net.planner.planet;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Objects;
import android.util.Log;

public class PlannerTask extends PlannerObject {
    private static final String TAG = "PlannerTask";

    private long deadline;
    private int maxSessionTimeInMinutes;
    private int maxDivisionsNumber;
    private int durationInMinutes;

    public PlannerTask(String title, long deadline, int durationInMinutes) {
        super(title);
        if (deadline < 0) {
            Log.e(TAG,"Invalid deadline");
            return;
        }
        if (durationInMinutes <= 0) {
            Log.e(TAG,"Invalid duration");
            return;
        }
        this.deadline = deadline;
        this.maxSessionTimeInMinutes = 24 * 60; //1 day as a default
        this.maxDivisionsNumber = 1; // in one go as a default
        this.durationInMinutes = durationInMinutes;
    }

    public long getDeadline() {
        return deadline;
    }

    public boolean setDeadline(long deadline) {
        if (deadline < 0) {
            Log.e(TAG,"Invalid deadline");
            return false;
        }
        this.deadline = deadline;
        return true;
    }

    public int getMaxSessionTimeInMinutes() {
        return maxSessionTimeInMinutes;
    }

    public boolean setMaxSessionTimeInMinutes(int maxSessionTimeInMinutes) {
        // TODO define defaults
        this.maxSessionTimeInMinutes = maxSessionTimeInMinutes;
        return true;
    }

    public int getMaxDivisionsNumber() {
        return maxDivisionsNumber;
    }

    public boolean setMaxDivisionsNumber(int maxDivisionsNumber) {
        if (maxDivisionsNumber < 1) {
            Log.e(TAG,"At least one instance of task should be allowed");
            return false;
        }
        this.maxDivisionsNumber = maxDivisionsNumber;
        return true;
    }

    public long getDurationInMinutes() {
        return durationInMinutes;
    }

    public long getDurationInMillis() {
        return durationInMinutes * 60000L;
    }

    public boolean setDurationInMinutes(int durationInMinutes) {
        if (durationInMinutes < 0) {
            Log.e(TAG,"Invalid task duration");
            return false;
        }
        this.durationInMinutes = durationInMinutes;
        return true;
    }

    @NotNull
    @Override
    public String toString() {
        String stringRep = super.toString();
        stringRep += "; Deadline is " + new Date(this.deadline) +
                "; Maximal time of one session (if divided) is " + maxSessionTimeInMinutes +
                "; Maximal number of divisions (if divided) is " + maxDivisionsNumber +
                " Expected duration of the task in milliseconds is: " + durationInMinutes;
        return stringRep + ".";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlannerTask)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PlannerTask that = (PlannerTask) o;
        return getDeadline() == that.getDeadline() &&
                getMaxSessionTimeInMinutes() == that.getMaxSessionTimeInMinutes() &&
                getDurationInMinutes() == that.getDurationInMinutes() &&
                getMaxDivisionsNumber() == that.getMaxDivisionsNumber();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDurationInMinutes(), getDeadline(),
                getMaxSessionTimeInMinutes(),
                getMaxDivisionsNumber());
    }
}
