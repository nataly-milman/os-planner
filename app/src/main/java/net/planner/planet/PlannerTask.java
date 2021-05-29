package net.planner.planet;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Objects;

public class PlannerTask extends PlannerObject {
    private long deadline;
    private int maxSessionTime; //in minutes
    private int maxDivisionsNumber;
    private long duration;

    public PlannerTask(String title, long deadline, long duration) {
        super(title);
        if (deadline < 0) {
            throw new IllegalArgumentException("Invalid deadline");
        }
        this.deadline = deadline;
        this.maxSessionTime = 24 * 60; //1 day as a default
        this.maxDivisionsNumber = 1; // in one go as a default
        this.duration = duration;
    }


    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public int getMaxSessionTime() {
        return maxSessionTime;
    }

    public void setMaxSessionTime(int maxSessionTime) {
        this.maxSessionTime = maxSessionTime;
    }

    public int getMaxDivisionsNumber() {
        return maxDivisionsNumber;
    }

    public void setMaxDivisionsNumber(int maxDivisionsNumber) {
        this.maxDivisionsNumber = maxDivisionsNumber;
    }

    public long getDuration() {
        return duration;
    }

    @NotNull
    @Override
    public String toString() {
        String stringRep = super.toString();
        stringRep += "; Deadline is " + new Date(this.deadline) +
                "; Maximal time of one session (if divided) is " + maxSessionTime +
                "; Maximal number of divisions (if divided) is " + maxDivisionsNumber +
                " Expected duration of the task in milliseconds is: " + duration;
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
                getDuration() == that.getDuration() &&
                getMaxSessionTime() == that.getMaxSessionTime() &&
                getMaxDivisionsNumber() == that.getMaxDivisionsNumber();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDuration(), getDeadline(), getMaxSessionTime(),
                getMaxDivisionsNumber());
    }
}
