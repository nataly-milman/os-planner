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

    // constructor
    /** Create PlannerTag from its title **/
    public PlannerTag(String tagName) {
        this.tagName = tagName;
        this.forbiddenTimeIntervals = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG)
                .collectIntervals(interval -> new ListIntervalCollection()).build();
        this.preferredTimeIntervals = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG)
                .collectIntervals(interval -> new ListIntervalCollection()).build();
    }

    //methods
    /** Get the name of the tag **/
    public String getTagName() {
        return tagName;
    }

    /** Set the name of the tag **/
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /** Get priority of the tag **/
    public int getPriority() {
        return priority;
    }

    /** Set priority of the tag as integer from 1 to 10 **/
    public boolean setPriority(int priority) {
        if (priority < 1 || priority > 10) {
            Log.e(TAG,"Illegal priority: Priority is integer from 1 to 10");
            return false;
        }
        this.priority = priority;
        return true;
    }

    /** Get time intervals during which it's forbidden to create tasks tagged with it **/
    public List<IInterval> getForbiddenTimeIntervals() {
        return new ArrayList<>(forbiddenTimeIntervals);
    }

    /** Get iterator over time intervals in which it's forbidden to create tasks tagged with it **/
    public Iterator<IInterval> getForbiddenTimeIntervalsIterator() {
        return forbiddenTimeIntervals.iterator();
    }

    /** Get tree with the time intervals in which it's forbidden to create tasks tagged with it **/
    public final IntervalTree getForbiddenTimeIntervalsTree() {
        return forbiddenTimeIntervals;
    }

    /** Add a time interval during which it's forbidden to create tasks tagged with it **/
    public boolean addForbiddenTimeInterval(long from, long until) {
        if (until < from) {
            Log.e(TAG,"Illegal time interval: Event cannot end before it starts");
            return false;
        }
        this.forbiddenTimeIntervals.add(new LongInterval(from, until));
        return true;
    }

    /** Get time intervals during which it's preferred to create tasks tagged with it **/
    public List<IInterval> getPreferredTimeIntervals() {
        return new ArrayList<>(preferredTimeIntervals);
    }

    /** Get iterator over time intervals in which it's preferred to create tasks tagged with it **/
    public Iterator<IInterval> getPreferredTimeIntervalsIterator() {
        return preferredTimeIntervals.iterator();
    }

    /** Get tree with the time intervals in which it's preferred to create tasks tagged with it **/
    public final IntervalTree getPreferredTimeIntervalsTree() {
        return preferredTimeIntervals;
    }

    /** Add a time interval during which it's preferred to create tasks tagged with it **/
    public boolean addPreferredTimeInterval(long from, long until) {
        if (until < from) {
            Log.e(TAG,"Illegal time interval: Event cannot end before it starts");
            return false;
        }
        this.preferredTimeIntervals.add(new LongInterval(from, until));
        return true;
    }

    /** Get forbidden for this tag time intervals that collide with the given one **/
    public Collection<?> getForbiddenCollisions(long startDate, long endDate) {
        return forbiddenTimeIntervals.overlap(new LongInterval(startDate, endDate));
    }

    /** Get preferred for this tag time intervals that collide with the given one **/
    public Collection<?> getPreferredCollisions(long startDate, long endDate) {
        return preferredTimeIntervals.overlap(new LongInterval(startDate, endDate));
    }

    /** Return whether or not the given time interval is forbidden for this tag **/
    public boolean isIntervalForbidden(long startDate, long endDate) {
        return getForbiddenCollisions(startDate, endDate).isEmpty();
    }

    /** Return whether or not the given time interval is preferred for this tag **/
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
