package net.planner.planet;

import com.brein.time.exceptions.IllegalTimeInterval;
import com.brein.time.exceptions.IllegalTimePoint;
import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder;
import com.brein.time.timeintervals.intervals.IInterval;
import com.brein.time.timeintervals.intervals.LongInterval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlannerCalendar {

    // Constants
    private static final int MAX_DAYS = 30; // Amount of days in a PlannerCalendar object.
    private static final int SPACE_IN_MINUTES = 15;
    private static final int MIN_SPACE_IN_SECONDS = 1;
    private static final long MIN_SPACE_IN_MILLIS = MIN_SPACE_IN_SECONDS * 1000L;
    public static final long RECOMMENDED_SPACE_IN_MILLIS = SPACE_IN_MINUTES * 60000L;

    // Fields
    private long startTime; // This calendar starts from this time (ms) and ends 30 days after it.
    private long spaceBetweenTasks;
    private IntervalTree thisMonth;
    private HashMap<String, PlannerTag> tags;

    // Constructors
    public PlannerCalendar() {
        init(System.currentTimeMillis(), MIN_SPACE_IN_MILLIS, Collections.emptyList(), Collections.emptyList());
    }

    public PlannerCalendar(long timeInMillis) {
        init(timeInMillis, MIN_SPACE_IN_MILLIS, Collections.emptyList(), Collections.emptyList());
    }

    public PlannerCalendar(long timeInMillis, long spaceBetweenTasks) {
        if (spaceBetweenTasks < MIN_SPACE_IN_MILLIS) spaceBetweenTasks = MIN_SPACE_IN_MILLIS;

        init(timeInMillis, spaceBetweenTasks, Collections.emptyList(), Collections.emptyList());
    }

    public PlannerCalendar(long timeInMillis, long spaceBetweenTasks, List<PlannerEvent> eventList) {
        if (spaceBetweenTasks < MIN_SPACE_IN_MILLIS) spaceBetweenTasks = MIN_SPACE_IN_MILLIS;
        if (eventList == null) eventList = Collections.emptyList();

        init(timeInMillis, spaceBetweenTasks, eventList, Collections.emptyList());
    }

    public PlannerCalendar(long timeInMillis, long spaceBetweenTasks, List<PlannerEvent> eventList, List<PlannerTag> newTags) {
        if (spaceBetweenTasks < MIN_SPACE_IN_MILLIS) spaceBetweenTasks = MIN_SPACE_IN_MILLIS;
        if (eventList == null) eventList = Collections.emptyList();
        if (newTags == null) newTags = Collections.emptyList();

        init(timeInMillis, spaceBetweenTasks, eventList, newTags);
    }

    private void init(long timeInMillis, long spaceBetweenTasks, List<PlannerEvent> eventList, List<PlannerTag> tagList) {
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
        thisMonth = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG).build();
        for (PlannerEvent event : eventList) {
            insertEvent(event);
        }

        // Define space between tasks.
        this.spaceBetweenTasks = spaceBetweenTasks;
    }

    // Methods
    public Collection<IInterval> getCollisions(long startDate, long endDate) {
        return thisMonth.overlap(new LongInterval(startDate, endDate));
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
        return !thisMonth.contains(toInsert) && thisMonth.add(toInsert);
    }

    public boolean forceInsertEvent(PlannerEvent event) {
        if (!isValidDate(event.getStartTime()) || !isValidDate(event.getEndTime())) {
            return false;
        }

        OccupiedInterval toInsert = new OccupiedInterval(event);
        return thisMonth.add(toInsert);
    }

    private long getSpacedStartTime(LongInterval interval) {
        long startTime = interval.getStart();
        long maxEnd = startTime - spaceBetweenTasks;
        Collection<IInterval> untaggedCollisions = getCollisions(maxEnd, startTime);
        for (IInterval untaggedGeneric : untaggedCollisions) {
            LongInterval untagged = (LongInterval) untaggedGeneric;
            long end = untagged.getEnd();
            if (end > maxEnd) {
                maxEnd = end;
            }
        }
        return maxEnd + spaceBetweenTasks;
    }

    private Collection<IInterval> mergeOverlapping(Collection<IInterval> intervals) {
        if (intervals.size() <= 1) {
            return intervals;
        }
        Iterator<IInterval> it = intervals.iterator();
        LinkedList<IInterval> merged = new LinkedList<>();

        LongInterval previous = (LongInterval) it.next();
        while (it.hasNext()) {
            LongInterval current = (LongInterval) it.next();
            if (previous.irBefore(current)) {
                merged.add(previous);
                previous = current;
            } else {
                previous = new LongInterval(previous.getStart(), current.getEnd());
            }

        }
        merged.add(previous);

        return merged;
    }

    // todo add task splitting
    private boolean insertUntaggedTaskHelper(PlannerTask task, Iterator<IInterval> possibleIterator) {
        long desiredDuration = task.getDurationInMillis() + spaceBetweenTasks;

        // Iterate over possible intervals.
        while (possibleIterator.hasNext()) {
            LongInterval possibleInterval = (LongInterval) possibleIterator.next();

            // Find first possible starting time in possible interval
            long startTime = getSpacedStartTime(possibleInterval);

            // Check if tagged interval is long enough.
            long possibleDuration = possibleInterval.getEnd() - startTime;
            if (possibleDuration >= desiredDuration) {
                return thisMonth.add(new OccupiedInterval(task, startTime, startTime + desiredDuration));
            }
        }
        return false;
    }

    private boolean insertTaskHelper(PlannerTask task, Iterator<IInterval> possibleIterator, IntervalTree collisionTree) {
        long desiredDuration = task.getDurationInMillis() + spaceBetweenTasks;

        // Iterate over possible intervals.
        while (possibleIterator.hasNext()) {
            LongInterval possibleInterval = (LongInterval) possibleIterator.next();

            // Find first possible starting time in possible interval
            long startTime = getSpacedStartTime(possibleInterval);

            // Check if tagged interval is long enough.
            long possibleDuration = possibleInterval.getEnd() - startTime;
            if (possibleDuration < desiredDuration) {
                continue;
            }

            // Find collisions in possible interval.
            Collection<IInterval> collisions = mergeOverlapping(collisionTree.overlap(possibleInterval));
            if (collisions.isEmpty()) {
                // The tagged interval is free and its long enough so we can push here.
                return thisMonth.add(new OccupiedInterval(task, startTime, startTime + desiredDuration));
            }

            //  Check if we can push task in between a pair of collision intervals.
            for (IInterval collision : collisions) {
                OccupiedInterval toCheck = new OccupiedInterval(task, startTime, startTime + desiredDuration);
                if (toCheck.irBefore(collision)) {
                    // Found free interval before some event/task so we can push here.
                    return thisMonth.add(toCheck);
                }
            }
        }
        return false;
    }

    public boolean preferredInsertTask(PlannerTask task) {
        PlannerTag tag = tags.get(task.getTagName());
        if (tag == null) {
            return false;
        }

        return insertTaskHelper(task, tag.getPreferredTimeIntervalsIterator(), thisMonth);
    }

    public boolean insertTask(PlannerTask task) {
        FreeTimeIterator freeTimeIt = new FreeTimeIterator();
        PlannerTag tag = tags.get(task.getTagName());
        if (tag == null) {
            return insertUntaggedTaskHelper(task, freeTimeIt);
        }

        return insertTaskHelper(task, freeTimeIt, tag.getForbiddenTimeIntervalsTree());
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
        if (!tags.containsKey(tagName)) {
            return false;
        }

        tags.remove(tagName);
        return true;
    }

    public PlannerTag getTag(String tagName) {
        return tags.get(tagName);
    }

    public List<String> getTagNames() {
        return new ArrayList<>(tags.keySet());
    }

    public List<PlannerTag> getTags() {
        return new ArrayList<>(tags.values());
    }

    // Helper functions
    private boolean isValidDate(long time) {
        long diffInMillis = time - startTime;
        return diffInMillis >= 0 &&
                TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) <= MAX_DAYS;
    }

    public long getStartTime() {
        return startTime;
    }

    // Inner classes
    private class FreeTimeIterator implements Iterator<IInterval> {

        private final Iterator<IInterval> occupiedIt;
        private long startTime;
        private LongInterval nextOccupied;

        public FreeTimeIterator() {
            occupiedIt = thisMonth.iterator();
            startTime = PlannerCalendar.this.startTime + spaceBetweenTasks;

            if (occupiedIt.hasNext()) {
                nextOccupied = (LongInterval) occupiedIt.next();
            } else {
                nextOccupied = null;
            }
        }

        @Override
        public boolean hasNext() {
            return nextOccupied != null;
        }

        // On-the-fly interval merging
        private LongInterval getNextOccupied() {
            LongInterval previous = nextOccupied;
            if (!occupiedIt.hasNext()) {
                nextOccupied = null;
                return previous;
            }

            while (occupiedIt.hasNext()) {
                nextOccupied = (LongInterval) occupiedIt.next();
                if (nextOccupied.irAfter(nextOccupied)) {
                    return previous;
                } else {
                    previous = new LongInterval(previous.getStart(), nextOccupied.getEnd());
                }
            }

            return nextOccupied;
        }

        @Override
        public IInterval next() {
            while (true) {
                LongInterval current = getNextOccupied();
                if (current == null) {
                    return null;
                }

                long endTime = current.getStart() - MIN_SPACE_IN_MILLIS;
                if (endTime > startTime) {
                    LongInterval free = new LongInterval(startTime, endTime);
                    startTime = current.getEnd() + spaceBetweenTasks;
                    return free;
                }
            }
        }
    }

    private static class OccupiedInterval extends LongInterval {

        public PlannerObject object;

        public OccupiedInterval() {
            super();
            this.object = null;
        }

        public OccupiedInterval(PlannerEvent event) throws IllegalTimeInterval, IllegalTimePoint {
            super(event.getStartTime(), event.getStartTime(), false, false);
            this.object = event;
        }

        public OccupiedInterval(PlannerTask task, long startDate, long endDate) throws IllegalTimeInterval, IllegalTimePoint {
            super(startDate, endDate, false, false);
            this.object = task;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            OccupiedInterval that = (OccupiedInterval) o;
            return object.title.equals(that.object.title);
        }
    }

}
