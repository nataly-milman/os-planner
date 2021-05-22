package net.planner.planet;

import android.icu.util.DateInterval;

import org.jetbrains.annotations.NotNull;

import java.time.DateTimeException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


public class PlannerEvent {
    // fields
    private static final String NO_TITLE = "(No title)";
    private String title;
    private String description;
    private long startTime;
    private long endTime;
    private boolean exclusiveForItsTimeSlot;

    private String location; // string for now
    private int reminder; // N minutes before (or some set values as in GC)
    private int priority; //1-10
    private PlannerTag tag;

    // constructors
    public PlannerEvent(String title, long startTime, long endTime) {
        this.title = title;
        if (title == null || title.isEmpty()) {
            this.title = NO_TITLE;
        }
        this.description = "";
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = ""; // for now string
        this.reminder = -1;
        this.priority = 5;
        this.tag = null;
        this.exclusiveForItsTimeSlot = true;
    }

    // methods
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.isEmpty()) {
            this.title = NO_TITLE;
        } else {
            this.title = title;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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
            throw new IllegalArgumentException("Illegal end time: Event cannot end before it starts");
        }
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getReminder() {
        return reminder;
    }

    public void setReminder(int reminder) {
        if (reminder < 0) {
            this.reminder = -1;
        } else {
            this.reminder = reminder;
        }
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if (priority < 1 || priority > 10) {
            throw new IllegalArgumentException("Illegal priority: Priority is integer from 1 to 10");
        }
        this.priority = priority;
    }

    public PlannerTag getTag() {
        return tag;
    }

    public void setTag(PlannerTag tag) {
        this.tag = tag;
    }

    public boolean isExclusiveForItsTimeSlot() {
        return exclusiveForItsTimeSlot;
    }

    public void setExclusiveForItsTimeSlot(boolean exclusiveForItsTimeSlot) {
        this.exclusiveForItsTimeSlot = exclusiveForItsTimeSlot;
    }

    @NotNull
    @Override
    public String toString() {
        String stringRep = "Title: " + this.title;
        if (!this.description.isEmpty()) {
            stringRep += "; Description: " + this.description;
        }
        stringRep += "; Starts at " + new Date(this.startTime) + "; Ends at " + new Date(this.endTime);
        if (!this.location.isEmpty()) {
            stringRep += "; Located at: " + location;
        }
        if (this.reminder != -1) {
            stringRep += "; Remind before: " + this.reminder + " minutes";
        }
        if (this.priority != -1) {
            stringRep += "; Priority: " + this.priority + "/10";
        }
        if (this.tag != null) {
            stringRep += "; Tagged: " + this.tag.toString();
        }
        if (this.exclusiveForItsTimeSlot){
            stringRep += "; Exclusive for this time slot";
        } else {
            stringRep += "; Not exclusive for this time slot";
        }
        return stringRep + ".";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlannerEvent)) return false;
        PlannerEvent that = (PlannerEvent) o;
        return reminder == that.reminder &&
                priority == that.priority &&
                title.equals(that.title) &&
                description.equals(that.description) &&
                startTime == that.startTime &&
                endTime == that.endTime &&
                location.equals(that.location) &&
                tag.equals(that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, startTime, endTime, location, reminder, priority, tag);
    }
}
