package net.planner.planet;

import com.brein.time.exceptions.IllegalTimeInterval;
import com.brein.time.exceptions.IllegalTimePoint;
import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder;
import com.brein.time.timeintervals.indexes.IntervalTreeNode;
import com.brein.time.timeintervals.intervals.IInterval;
import com.brein.time.timeintervals.intervals.LongInterval;
import com.brein.time.timeintervals.intervals.NumberInterval;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import kotlin.Pair;

public class PlannerCalendar {

    // Constants
    private static final int MAX_DAYS = 30; // Amount of days in a PlannerCalendar object.
    private static final int SLOT_SIZE = 15; // Minimum number of minutes for an event.

    // Fields
    private long startTime; // This calendar starts from this time (ms) and ends 30 days after it.
    private IntervalTree thisMonth;
    private HashMap<String, PlannerTag> tags;

    // Constructors
    public PlannerCalendar() {
        init(System.currentTimeMillis(), Collections.emptyList(), Collections.emptyList());
    }

    public PlannerCalendar(long timeInMillis) {
        init(timeInMillis, Collections.emptyList(), Collections.emptyList());
    }

    public PlannerCalendar(long timeInMillis, List<PlannerEvent> eventList) {
        if (eventList == null) eventList = Collections.emptyList();

        init(timeInMillis, eventList, Collections.emptyList());
    }

    public PlannerCalendar(long timeInMillis, List<PlannerEvent> eventList, List<PlannerTag> newTags) {
        if (eventList == null) eventList = Collections.emptyList();
        if (newTags == null) newTags = Collections.emptyList();

        init(timeInMillis, eventList, newTags);
    }

    private void init(long timeInMillis, List<PlannerEvent> eventList, List<PlannerTag> tagList) {
        // Get time at start of day.
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(timeInMillis);
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);
        this.startTime = startDate.getTimeInMillis();

        // Add tags.
        tags = new HashMap<>(tagList.size());
        for (PlannerTag tag : tagList) {
            tags.put(tag.getTagName(), tag);
        }

        // Create occupiedTree and add events.
        thisMonth = IntervalTreeBuilder.newBuilder().usePredefinedType(IntervalTreeBuilder.IntervalType.LONG).build();
        for (PlannerEvent event : eventList) {
            insertEvent(event);
        }
    }

    // Methods
    public Collection<?> getCollisions(long startDate, long endDate) {
        return thisMonth.find(new LongInterval(startDate, endDate));
    }

    public boolean isIntervalAvailable(long startDate, long endDate) {
        return getCollisions(startDate, endDate).isEmpty();
    }

    public boolean isIntervalTaggedForbidden(String tagName, long startDate, long endDate) {
        PlannerTag tag = tags.get(tagName);
        if (tag == null) {
            return false;
        }

        return tag.isIntervalForbidden(startDate, endDate);
    }

    public boolean isIntervalTaggedPreferred(String tagName, long startDate, long endDate) {
        PlannerTag tag = tags.get(tagName);
        if (tag == null) {
            return false;
        }

        return tag.isIntervalPreferred(startDate, endDate);
    }

    public boolean insertEvent(PlannerEvent event) {
        if (!isValidDate(event.getStartTime()) || !isValidDate(event.getEndTime())) {
            return false;
        }

        OccupiedInterval toInsert = new OccupiedInterval(event);
        boolean result =!thisMonth.contains(toInsert) && thisMonth.add(toInsert);
        if (result && !thisMonth.isBalanced()){
            thisMonth.balance();
        }
        return result;
    }

    public boolean forceInsertEvent(PlannerEvent event) {
        if (!isValidDate(event.getStartTime()) || !isValidDate(event.getEndTime())) {
            return false;
        }

        OccupiedInterval toInsert = new OccupiedInterval(event);
        boolean result = thisMonth.add(toInsert);
        if (result && !thisMonth.isBalanced()){
            thisMonth.balance();
        }
        return result;
    }

    public boolean insertTask(PlannerTask task) {
        // todo implement
        return false;
    }

    public boolean forceInsertTask(PlannerTask task) {
        // todo implement
        return false;
    }

    public boolean removeEvent(PlannerEvent event) {
        OccupiedInterval toRemove = new OccupiedInterval(event);
        return thisMonth.remove(toRemove);
    }

    public boolean containsTag(String tagName) {
        return tags.containsKey(tagName);
    }

    public boolean containsTag(PlannerTag tag) {
        return tags.containsKey(tag.getTagName());
    }

    public boolean addTag(PlannerTag tag) {
        String tagName = tag.getTagName();
        if (tags.containsKey(tagName)) {
            return false;
        }

        tags.put(tagName, tag);
        return true;
    }

    public boolean removeTag(String tagName) {
        if (tags.containsKey(tagName)) {
            return false;
        }

        tags.remove(tagName);
        return true;
    }

    public PlannerTag getTag(String tagName) {
        return tags.get(tagName);
    }

    // Helper functions
    private boolean isValidDate(long time) {
        long diffInMillis = time - startTime;
        return diffInMillis >= 0 &&
                TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) <= MAX_DAYS;
    }


    private int toSlotIndex(long time) {
        long diffInMillis = time - startTime;
        return (int) TimeUnit.MINUTES.convert(diffInMillis, TimeUnit.MILLISECONDS) / SLOT_SIZE;
    }

    // Inner classes
    private class OccupiedInterval extends NumberInterval<Long> {

        public PlannerObject object;

        public OccupiedInterval() {
            super();
            this.object = null;
        }

        public OccupiedInterval(PlannerEvent event) throws IllegalTimeInterval, IllegalTimePoint {
            super(Long.class, event.getStartTime(), event.getStartTime(), false, false);
            this.object = event;
        }

        public OccupiedInterval(PlannerTask task, long startDate, long endDate) throws IllegalTimeInterval, IllegalTimePoint {
            super(Long.class, startDate, endDate, false, false);
            this.object = task;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            OccupiedInterval that = (OccupiedInterval) o;
            return object.equals(that.object);
        }
    }

}
