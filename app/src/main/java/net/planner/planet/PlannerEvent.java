package net.planner.planet;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Objects;


public class PlannerEvent extends PlannerObject {
    private long startTime;
    private long endTime;
    private long eventId;
    private boolean isAllDay;
    private final PlannerTask parentTask;

    // constructors
    public PlannerEvent(String title, long startTime, long endTime) {
        super(title);
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventId = -1L;
        this.isAllDay = false; // @TODO check duration?
        this.parentTask = null;
    }

    public PlannerEvent(PlannerTask task, long startTime, long endTime) {
        super(task.title);
        this.title = task.title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventId = -1L;
        this.isAllDay = false; // @TODO check duration?
        this.parentTask = task;
    }

    // methods
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        if (startTime < 0) {
            throw new IllegalArgumentException("Illegal start time: Time cannot be negative");
        }
        long duration = this.endTime - this.startTime;
        this.startTime = startTime;
        this.endTime = startTime + duration;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        if (this.startTime > endTime) {
            throw new IllegalArgumentException(
                    "Illegal end time: Event cannot end before it starts");
        }
        this.endTime = endTime;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public void setAllDay(boolean isAllDay) {
        this.isAllDay = isAllDay;
    }

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
