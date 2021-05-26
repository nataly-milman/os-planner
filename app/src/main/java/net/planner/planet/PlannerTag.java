package net.planner.planet;

import android.util.Pair;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class PlannerTag {
    private String tagName;
    private LinkedList<long[]> forbiddenTimeIntervals;
    private LinkedList<long[]> preferredTimeIntervals;
    private int priority;

    public PlannerTag(String tagName) {
        this.tagName = tagName;
        this.forbiddenTimeIntervals = new LinkedList<>();
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if (priority < 1 || priority > 10) {
            throw new IllegalArgumentException(
                    "Illegal priority: Priority is integer from 1 to 10");
        }
        this.priority = priority;
    }

    public LinkedList<long[]> getForbiddenTimeIntervals() {
        return forbiddenTimeIntervals;
    }

    public void addForbiddenTimeInterval(long from, long until) {
        if (until < from) {
            throw new IllegalArgumentException(
                    "Illegal time interval: Event cannot end before it starts");
        }
        this.forbiddenTimeIntervals.add(new long[]{from, until});
    }

    public LinkedList<long[]> getPreferredTimeIntervals() {
        return preferredTimeIntervals;
    }

    public void addPreferredTimeInterval(long from, long until) {
        if (until < from) {
            throw new IllegalArgumentException(
                    "Illegal time interval: Event cannot end before it starts");
        }
        this.preferredTimeIntervals.add(new long[]{from, until});
    }
}
