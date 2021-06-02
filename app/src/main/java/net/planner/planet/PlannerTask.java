package net.planner.planet;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Objects;

public class PlannerTask extends PlannerObject {
    private long deadline;
    private int maxSessionTimeInMinutes;
    private int maxDivisionsNumber;
    private int durationInMinutes;

    public PlannerTask(String title, long deadline, int durationInMinutes) {
        super(title);
        if (deadline < 0) {
            throw new IllegalArgumentException("Invalid deadline");
        }
        if (durationInMinutes <= 0) {
            throw new IllegalArgumentException("Invalid duration");
        }
        this.deadline = deadline;
        this.maxSessionTimeInMinutes = 24 * 60; //1 day as a default
        this.maxDivisionsNumber = 1; // in one go as a default
        this.durationInMinutes = durationInMinutes;
    }


    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public int getMaxSessionTimeInMinutes() {
        return maxSessionTimeInMinutes;
    }

    public void setMaxSessionTimeInMinutes(int maxSessionTimeInMinutes) {
        this.maxSessionTimeInMinutes = maxSessionTimeInMinutes;
    }

    public int getMaxDivisionsNumber() {
        return maxDivisionsNumber;
    }

    public void setMaxDivisionsNumber(int maxDivisionsNumber) {
        this.maxDivisionsNumber = maxDivisionsNumber;
    }

    public long getDurationInMinutes() {
        return durationInMinutes;
    }

    public long getDurationInMilliseconds() {
        return durationInMinutes * 60000;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
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
