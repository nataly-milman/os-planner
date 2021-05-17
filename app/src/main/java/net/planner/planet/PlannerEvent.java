package net.planner.planet;

import org.jetbrains.annotations.NotNull;

import java.util.Date;


public class PlannerEvent {
    // fields
    private static final String NO_TITLE = "(No title)";
    private String title;
    private String description;
    private Date startTime;
    private Date endTime;

    private String location; // string for now
    // todo "private ??? repeat"; (repeat conditions, y/n)
    private int reminder; // N minutes before (or some set values as in GC)
    private int priority; //1-10
    private String tag; // list of tags?

    // constructors
    public PlannerEvent(String title, Date startTime, Date endTime) {
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
        this.tag = "";  // for now string
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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        if (startTime.after(this.endTime)) {
            // todo - a) forbid b) set end to start + min event length c) exception
            return;
        }
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        if (this.startTime.after(endTime)) {
            // todo - a) forbid b) set end to start + min event length c) exception
            return;
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
            return;
        }
        this.priority = priority;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @NotNull
    @Override
    public String toString() {
        String stringRep = "Title: " + this.title;
        if (!this.description.isEmpty()) {
            stringRep += "; Description: " + this.description;
        }
        stringRep += "; Starts at " + this.startTime + "; Ends at " + this.endTime;
        if (!this.location.isEmpty()) {
            stringRep += "; Located at: " + location;
        }
        if (this.reminder != -1) {
            stringRep += "; Remind before: " + this.reminder + " minutes";
        }
        if (this.priority != -1) {
            stringRep += "; Priority: " + this.priority + "/10";
        }
        if (!this.tag.isEmpty()) {
            stringRep += "; Tagged: " + this.tag;
        }
        return stringRep + ".";
    }

}
