package net.planner.planet;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

abstract class PlannerObject {
    private static final String TAG = "PlannerObject";
    public static final String NO_TAG = "NoTag";

    protected static final String NO_TITLE = "(No title)";
    protected String title;
    protected String description;
    protected boolean exclusiveForItsTimeSlot;

    protected String location; // string for now
    protected int reminder; // N minutes before (or some set values as in GC)
    protected String tag;

    //constructors
    /** Create PlannerObject from its title **/
    protected PlannerObject(String title) {
        this.title = title;
        if (title == null || title.isEmpty()) {
            this.title = NO_TITLE;
        }
        this.description = "";
        this.location = ""; // for now string
        this.reminder = -1;
        this.tag = NO_TAG;
        this.exclusiveForItsTimeSlot = true;
    }

    // validity check
    /** Input parameters validity check **/
    public static boolean isValid(int reminder) {
        // title, description, location, exclusiveForItsTimeSlot - no predefined ranges
        if (reminder < -1){
            Log.e(TAG, "Validation error: Reminder is the number of minutes, -1 for no reminder");
            return false;
        }
        return true;
    }

    // methods
    /** Get PlannerObject's title **/
    public String getTitle() {
        return title;
    }

    /** Set PlannerObject's title **/
    public void setTitle(String title) {
        if (title == null || title.isEmpty()) {
            this.title = NO_TITLE;
        } else {
            this.title = title;
        }
    }

    /** Get PlannerObject's description **/
    public String getDescription() {
        return description;
    }

    /** Set PlannerObject's description **/
    public void setDescription(String description) {
        this.description = description;
    }

    /** Get PlannerObject's location **/
    public String getLocation() {
        return location;
    }

    /** Set PlannerObject's location **/
    public void setLocation(String location) {
        this.location = location;
    }

    /** Get PlannerObject's reminder time in minutes, -1 for no reminder **/
    public int getReminder() {
        return reminder;
    }

    /** Set PlannerObject's reminder time in minutes, -1 for no reminder **/
    public void setReminder(int reminder) {
        if (reminder < 0) {
            this.reminder = -1;
        } else {
            this.reminder = reminder;
        }
    }

    /** Get the name of the tag object connected to this task **/
    public String getTagName() {
        return tag;
    }

    /** Set the name of the tag object connected to this task **/
    public void setTagName(String tag) {
        this.tag = tag;
    }

    /** Get whether or not some other event can be defined at the same time with this **/
    public boolean isExclusiveForItsTimeSlot() {
        return exclusiveForItsTimeSlot;
    }

    /** Set whether or not some other event can be defined at the same time with this **/
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
