package net.planner.planet;

import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder;
import com.brein.time.timeintervals.intervals.IInterval;
import com.brein.time.timeintervals.intervals.LongInterval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PlannerTag {
    private String tagName;
    private IntervalTree forbiddenTimeIntervals;
    private IntervalTree preferredTimeIntervals;
    private int priority;

    public PlannerTag(String tagName) {
        this.tagName = tagName;
        this.forbiddenTimeIntervals = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG).build();
        this.preferredTimeIntervals = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG).build();
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

    public List<IInterval> getForbiddenTimeIntervals() {
        return new ArrayList<>(forbiddenTimeIntervals);
    }

    public void addForbiddenTimeInterval(long from, long until) {
        if (until < from) {
            throw new IllegalArgumentException(
                    "Illegal time interval: Event cannot end before it starts");
        }
        this.forbiddenTimeIntervals.add(new LongInterval(from, until));
    }

    public List<IInterval> getPreferredTimeIntervals() {
        return new ArrayList<>(preferredTimeIntervals);
    }

    public void addPreferredTimeInterval(long from, long until) {
        if (until < from) {
            throw new IllegalArgumentException(
                    "Illegal time interval: Event cannot end before it starts");
        }
        this.preferredTimeIntervals.add(new LongInterval(from, until));
    }

    public Collection<?> getForbiddenCollisions(long startDate, long endDate) {
        return forbiddenTimeIntervals.find(new LongInterval(startDate, endDate));
    }

    public Collection<?> getPreferredCollisions(long startDate, long endDate) {
        return preferredTimeIntervals.find(new LongInterval(startDate, endDate));
    }

    public boolean isIntervalForbidden(long startDate, long endDate) {
        return getForbiddenCollisions(startDate, endDate).isEmpty();
    }

    public boolean isIntervalPreferred(long startDate, long endDate) {
        return getPreferredCollisions(startDate, endDate).isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlannerTag that = (PlannerTag) o;
        return tagName.equals(that.tagName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagName);

    }
}
