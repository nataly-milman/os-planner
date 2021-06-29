package net.planner.planet;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

abstract class PlannerObject {
    private static final String TAG = "PlannerObject";
    protected static final String NO_TITLE = "(No title)";
    protected String title;
    protected String description;
    protected boolean exclusiveForItsTimeSlot;

    protected String location; // string for now
    protected int reminder; // N minutes before (or some set values as in GC)
    protected String tag;

    //constructors
    protected PlannerObject(String title) {
        super();
        this.title = title;
        if (title == null || title.isEmpty()) {
            this.title = NO_TITLE;
        }
        this.description = "";
        this.location = ""; // for now string
        this.reminder = -1;
        this.tag = "NoTag";
        this.exclusiveForItsTimeSlot = true;
    }

    // validity check
    public static boolean isValid(int reminder) {
        // title, description, location, exclusiveForItsTimeSlot - no predefined ranges
        if (reminder < -1){
            Log.e(TAG, "Validation error: Reminder is the number of minutes, -1 for no reminder");
            return false;
        }
        return true;
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

    public String getTagName() {
        return tag;
    }

    public void setTagName(String tag) {
        this.tag = tag;
    }

    public boolean isExclusiveForItsTimeSlot() {
        return exclusiveForItsTimeSlot;
    }

    public void setExclusiveForItsTimeSlot(boolean exclusiveForItsTimeSlot) {
        this.exclusiveForItsTimeSlot = exclusiveForItsTimeSlot;
    }

    @NotNull @Override public String toString() {
        String stringRep = "Title: " + this.title;
        if (!this.description.isEmpty()) {
            stringRep += "; Description: " + this.description;
        }
        if (!this.location.isEmpty()) {
            stringRep += "; Located at: " + location;
        }
        if (this.reminder != -1) {
            stringRep += "; Remind before: " + this.reminder + " minutes";
        }
        if (this.tag != null) {
            stringRep += "; Tagged: " + this.tag;
        }
        if (this.exclusiveForItsTimeSlot) {
            stringRep += "; Exclusive for this time slot";
        } else {
            stringRep += "; Not exclusive for this time slot";
        }
        return stringRep;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlannerObject)) {
            return false;
        }
        PlannerObject that = (PlannerObject) o;
        return isExclusiveForItsTimeSlot() == that
                .isExclusiveForItsTimeSlot() && getReminder() == that
                .getReminder() && getTitle()
                       .equals(that.getTitle()) && getDescription()
                       .equals(that.getDescription()) && getLocation()
                       .equals(that.getLocation()) && getTagName().equals(that.getTagName());
    }

    @Override public int hashCode() {
        return Objects
                .hash(getTitle(), getDescription(), isExclusiveForItsTimeSlot(), getLocation(), getReminder(), getTagName());
    }
}
