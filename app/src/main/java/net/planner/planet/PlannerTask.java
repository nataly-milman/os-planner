package net.planner.planet;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Objects;

public class PlannerTask extends PlannerObject {
    private static final String TAG = "PlannerTask";

    private long deadline;
    protected int priority; //1-10
    private int maxSessionTimeInMinutes;
    private int maxDivisionsNumber;
    private int durationInMinutes;

    public PlannerTask(String title, long deadline, int durationInMinutes) {
        super(title);
        if (deadline < 0) {
            Log.e(TAG, "Invalid deadline");
            return;
        }
        if (durationInMinutes <= 0) {
            Log.e(TAG, "Invalid duration");
            return;
        }
        this.deadline = deadline;
        this.maxSessionTimeInMinutes = 60; //1 hour as a default
        this.maxDivisionsNumber = 1; // in one go as a default
        this.durationInMinutes = durationInMinutes;
        this.priority = 5;
    }

    // validity check
    public static boolean isValid(int reminder, int priority, long deadline,
                                  int durationInMinutes, int maxSessionTimeInMinutes,
                                  int maxDivisionsNumber) {
        if (!PlannerObject.isValid(reminder)) {
            return false;
        }
        if (priority < 1 || priority > 10) {
            Log.e(TAG, "Validation error: Priority should be an integer from 1 to 10");
            return false;
        }

        if (durationInMinutes <= 0) {
            Log.e(TAG, "Validation error: invalid duration");
            return false;
        }

        if (maxDivisionsNumber < 1) {
            Log.e(TAG, "Validation error: At least one instance of task should be allowed");
            return false;
        }

        if (maxSessionTimeInMinutes < 15) {
            Log.e(TAG, "Validation error: Maximal session time has to be at least 15 min");
            return false;
        }

        if (maxDivisionsNumber * maxSessionTimeInMinutes < durationInMinutes) {
            Log.e(TAG, "Validation error: Under the constraints duration is unattainable");
            return false;
        }

        return true;
    }

    // methods
    public long getDeadline() {
        return deadline;
    }

    public boolean setDeadline(long deadline) {
        if (deadline < 0) {
            Log.e(TAG, "Invalid deadline");
            return false;
        }
        this.deadline = deadline;
        return true;
    }

    public int getPriority() {
        return priority;
    }

    public boolean setPriority(int priority) {
        if (priority < 1 || priority > 10) {
            Log.e("PlannerObject", "Illegal priority: Priority is an integer from 1 to 10");
            return false;
        }
        this.priority = priority;
        return true;
    }

    public int getMaxSessionTimeInMinutes() {
        return maxSessionTimeInMinutes;
    }

    public boolean setMaxSessionTimeInMinutes(int maxSessionTimeInMinutes) {
        if (maxSessionTimeInMinutes < 15) {
            Log.e(TAG, "Maximal session time has to be at least 15 min");
            return false;
        }
        this.maxSessionTimeInMinutes = maxSessionTimeInMinutes;
        return true;
    }

    public int getMaxDivisionsNumber() {
        return maxDivisionsNumber;
    }

    public boolean setMaxDivisionsNumber(int maxDivisionsNumber) {
        if (maxDivisionsNumber < 1) {
            Log.e(TAG, "At least one instance of task should be allowed");
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
            Log.e(TAG, "Invalid task duration");
            return false;
        }
        this.durationInMinutes = durationInMinutes;
        return true;
    }

    @NotNull @Override public String toString() {
        String stringRep = super.toString();
        if (this.priority != -1) {
            stringRep += "; Priority: " + this.priority + "/10";
        }
        stringRep += "; Deadline is " + new Date(this.deadline) +
                     "; Maximal time of one session (if divided) is " + maxSessionTimeInMinutes +
                     "; Maximal number of divisions (if divided) is " + maxDivisionsNumber +
                     "; Expected duration of the task in minutes is: " + durationInMinutes;
        return stringRep + ".";
    }

    @Override public boolean equals(Object o) {
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
               getMaxDivisionsNumber() == that.getMaxDivisionsNumber() &&
               getPriority() == that.getPriority();
    }

    @Override public int hashCode() {
        return Objects.hash(super.hashCode(), getDurationInMinutes(), getDeadline(),
                            getMaxSessionTimeInMinutes(), getMaxDivisionsNumber(), getPriority());
    }
}
