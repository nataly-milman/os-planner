package net.planner.planet;

import android.util.Log;

import com.brein.time.timeintervals.collections.ListIntervalCollection;
import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder;
import com.brein.time.timeintervals.intervals.IInterval;
import com.brein.time.timeintervals.intervals.LongInterval;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class PlannerTag {
    private static final String TAG = "PlannerTag";
    private String tagName;
    private IntervalTree forbiddenTimeIntervals;
    private IntervalTree preferredTimeIntervals;
    private int priority;

    public PlannerTag(String tagName) {
        this.tagName = tagName;
        this.forbiddenTimeIntervals = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG)
                .collectIntervals(interval -> new ListIntervalCollection()).build();
        this.preferredTimeIntervals = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG)
                .collectIntervals(interval -> new ListIntervalCollection()).build();
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

    public boolean setPriority(int priority) {
        if (priority < 1 || priority > 10) {
            Log.e(TAG,"Illegal priority: Priority is integer from 1 to 10");
            return false;
        }
        this.priority = priority;
        return true;
    }

    public List<IInterval> getForbiddenTimeIntervals() {
        return new ArrayList<>(forbiddenTimeIntervals);
    }

    public Iterator<IInterval> getForbiddenTimeIntervalsIterator() {
        return forbiddenTimeIntervals.iterator();
    }

    public final IntervalTree getForbiddenTimeIntervalsTree() {
        return forbiddenTimeIntervals;
    }

    public boolean addForbiddenTimeInterval(long from, long until) {
        if (until < from) {
            Log.e(TAG,"Illegal time interval: Event cannot end before it starts");
            return false;
        }
        this.forbiddenTimeIntervals.add(new LongInterval(from, until));
        return true;
    }

    public List<IInterval> getPreferredTimeIntervals() {
        return new ArrayList<>(preferredTimeIntervals);
    }

    public Iterator<IInterval> getPreferredTimeIntervalsIterator() {
        return preferredTimeIntervals.iterator();
    }

    public final IntervalTree getPreferredTimeIntervalsTree() {
        return preferredTimeIntervals;
    }

    public boolean addPreferredTimeInterval(long from, long until) {
        if (until < from) {
            Log.e(TAG,"Illegal time interval: Event cannot end before it starts");
            return false;
        }
        this.preferredTimeIntervals.add(new LongInterval(from, until));
        return true;
    }

    public Collection<?> getForbiddenCollisions(long startDate, long endDate) {
        return forbiddenTimeIntervals.overlap(new LongInterval(startDate, endDate));
    }

    public Collection<?> getPreferredCollisions(long startDate, long endDate) {
        return preferredTimeIntervals.overlap(new LongInterval(startDate, endDate));
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
