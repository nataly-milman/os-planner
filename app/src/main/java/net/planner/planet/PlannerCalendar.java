package net.planner.planet;

import com.brein.time.exceptions.IllegalTimeInterval;
import com.brein.time.exceptions.IllegalTimePoint;
import com.brein.time.timeintervals.collections.ListIntervalCollection;
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

/**
 * The PlannerCalendar represents a calendar that can hold and tasks and events.
 */
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
    private IntervalTree occupiedTree;
    private HashMap<String, PlannerTag> tags;

    // Constructors

    /**
     * Construct a new calendar with the current system time as the start time.
     */
    public PlannerCalendar() {
        init(System.currentTimeMillis(), MIN_SPACE_IN_MILLIS, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Construct a new calendar with the given start time.
     */
    public PlannerCalendar(long timeInMillis) {
        init(timeInMillis, MIN_SPACE_IN_MILLIS, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Construct a new calendar with the given start time and the given space to leave between tasks.
     */
    public PlannerCalendar(long timeInMillis, long spaceBetweenTasks) {
        if (spaceBetweenTasks < MIN_SPACE_IN_MILLIS) spaceBetweenTasks = MIN_SPACE_IN_MILLIS;

        init(timeInMillis, spaceBetweenTasks, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Construct a new calendar with the given start time, the given space between tasks and with the given events.
     */
    public PlannerCalendar(long timeInMillis, long spaceBetweenTasks, List<PlannerEvent> eventList) {
        if (spaceBetweenTasks < MIN_SPACE_IN_MILLIS) spaceBetweenTasks = MIN_SPACE_IN_MILLIS;
        if (eventList == null) eventList = Collections.emptyList();

        init(timeInMillis, spaceBetweenTasks, eventList, Collections.emptyList());
    }

    /**
     * Construct a new calendar with given start time, given space between tasks, given events and given tags.
     */
    public PlannerCalendar(long timeInMillis, long spaceBetweenTasks, List<PlannerEvent> eventList, List<PlannerTag> newTags) {
        if (spaceBetweenTasks < MIN_SPACE_IN_MILLIS) spaceBetweenTasks = MIN_SPACE_IN_MILLIS;
        if (eventList == null) eventList = Collections.emptyList();
        if (newTags == null) newTags = Collections.emptyList();

        init(timeInMillis, spaceBetweenTasks, eventList, newTags);
    }

    /**
     * Helper function: Actual constructor (receives default values from other constructors).
     */
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
            String tagName = tag.getTagName();
            if (!tagName.equals(PlannerObject.NO_TAG)) {
                tags.put(tagName, tag);
            }
        }

        // Create occupiedTree and add events.
        occupiedTree = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG)
                .collectIntervals(interval -> new ListIntervalCollection()).build();
        for (PlannerEvent event : eventList) {
            insertEvent(event);
        }

        // Define space between tasks.
        this.spaceBetweenTasks = spaceBetweenTasks;
    }

    // Methods

    /**
     * Returns the start time for this calendar (all dates have to be after this one).
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns all intervals in the calendar that overlap with [startDate, endDate].
     */
    public Collection<IInterval> getCollisions(long startDate, long endDate) {
        return occupiedTree.overlap(new LongInterval(startDate, endDate));
    }

    /**
     * Returns true if the interval [startDate, endDate] doesn't overlap with any interval in this calendar.
     */
    public boolean isIntervalAvailable(long startDate, long endDate) {
        return getCollisions(startDate, endDate).isEmpty();
    }

    /**
     * Returns true if the interval [startDate, endDate] is tagged as forbidden by a tag named tagName in this calendar.
     */
    public boolean isIntervalTaggedForbidden(String tagName, long startDate, long endDate) {
        PlannerTag tag = safeGetTag(tagName);
        if (tag == null) {
            return false;
        }

        return tag.isIntervalForbidden(startDate, endDate);
    }

    /**
     * Returns true if the interval [startDate, endDate] is tagged as preferred by a tag named tagName in this calendar.
     */
    public boolean isIntervalTaggedPreferred(String tagName, long startDate, long endDate) {
        PlannerTag tag = safeGetTag(tagName);
        if (tag == null) {
            return false;
        }

        return tag.isIntervalPreferred(startDate, endDate);
    }

    /**
     * Attempts to insert the given event into this calendar (interval must not overlap). Returns true if successful.
     */
    public boolean insertEvent(PlannerEvent event) {
        if (!isValidDate(event.getStartTime()) || !isValidDate(event.getEndTime())) {
            return false;
        }

        OccupiedInterval toInsert = new OccupiedInterval(event);
        return !occupiedTree.contains(toInsert) && occupiedTree.add(toInsert);
    }

    /**
     * Attempts to insert the given event into this calendar (can overlap with others). Returns true if successful.
     */
    public boolean forceInsertEvent(PlannerEvent event) {
        if (!isValidDate(event.getStartTime()) || !isValidDate(event.getEndTime())) {
            return false;
        }

        OccupiedInterval toInsert = new OccupiedInterval(event);
        return occupiedTree.add(toInsert);
    }

    /**
     * Inserts a tagged task into the calendar at the first preferred free time. Returns events it was assigned to. On failure, returns empty list.
     */
    public List<PlannerEvent> preferredInsertTask(PlannerTask task) {
        PlannerTag tag = safeGetTag(task.getTagName());
        if (tag == null) {
            return new LinkedList<>();
        }

        return insertTaskHelper(task, tag.getPreferredTimeIntervalsIterator(), occupiedTree);
    }

    /**
     * Inserts a task into the calendar at the first non-forbidden free time. Returns events it was assigned to. On failure, returns empty list.
     */
    public List<PlannerEvent> insertTask(PlannerTask task) {
        FreeTimeIterator freeTimeIt = new FreeTimeIterator();
        PlannerTag tag = safeGetTag(task.getTagName());
        if (tag == null) {
            return insertUntaggedTaskHelper(task, freeTimeIt);
        }

        return insertTaskHelper(task, freeTimeIt, tag.getForbiddenTimeIntervalsTree());
    }

    /**
     * Removes the given event from this calendar. Return true if found.
     */
    public boolean removeEvent(PlannerEvent event) {
        OccupiedInterval toRemove = new OccupiedInterval(event);
        return occupiedTree.remove(toRemove);
    }

    /**
     * Returns true if this calendar contains a tag with the given name.
     */
    public boolean containsTag(String tagName) {
        return tags.containsKey(tagName);
    }

    /**
     * Returns true if this calendar contains a tag with the given name.
     */
    public boolean containsTag(PlannerTag tag) {
        return tags.containsKey(tag.getTagName());
    }

    /**
     * Attempts to add the given tag to the calendar. Returns true if successful (if name wasn't given already).
     */
    public boolean addTag(PlannerTag tag) {
        String tagName = tag.getTagName();
        if (tags.containsKey(tagName)) {
            return false;
        }

        tags.put(tagName, tag);
        return true;
    }

    /**
     * Removes the tag with the given name from this calendar. Return true if found.
     */
    public boolean removeTag(String tagName) {
        if (!tags.containsKey(tagName)) {
            return false;
        }

        tags.remove(tagName);
        return true;
    }

    /**
     * Returns the tag with the given name from this calendar. Return null if not found.
     */
    public PlannerTag getTag(String tagName) {
        return tags.get(tagName);
    }

    /**
     * Returns the names of all the tags in this calendar.
     */
    public List<String> getTagNames() {
        return new ArrayList<>(tags.keySet());
    }

    /**
     * Returns all the tags in this calendar.
     */
    public List<PlannerTag> getTags() {
        return new ArrayList<>(tags.values());
    }

    // Helper functions

    /**
     * Helper function: Returns the first time in the calendar where the start doesn't overlap with others.
     */
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

    /**
     * Helper function: Returns a collection where all overlapping intervals have been merged.
     */
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

    /**
     * Helper function: Inserts an untagged task into the calendar at the first free time. Returns events it was assigned to.
     */
    private LinkedList<PlannerEvent> insertUntaggedTaskHelper(PlannerTask task, Iterator<IInterval> possibleIterator) {
        LinkedList<PlannerEvent> assignments = new LinkedList<>();
        long desiredDuration = task.getDurationInMillis() + spaceBetweenTasks;

        // Iterate over possible intervals.
        while (possibleIterator.hasNext()) {
            LongInterval possibleInterval = (LongInterval) possibleIterator.next();

            // Find first possible starting time in possible interval
            long startTime = getSpacedStartTime(possibleInterval);

            // Check if tagged interval is long enough.
            long possibleDuration = possibleInterval.getEnd() - startTime;
            if (possibleDuration >= desiredDuration) {
                PlannerEvent toAdd = new PlannerEvent(task, startTime, startTime + desiredDuration);
                assignments.add(toAdd);
                occupiedTree.add(new OccupiedInterval(toAdd));
                return assignments;
            }
        }
        return assignments;
    }

    /**
     * Helper function: Inserts a task into the calendar at the first possible time that doesn't collide. Returns events it was assigned to.
     */
    private LinkedList<PlannerEvent> insertTaskHelper(PlannerTask task, Iterator<IInterval> possibleIterator, IntervalTree collisionTree) {
        LinkedList<PlannerEvent> assignments = new LinkedList<>();
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
                PlannerEvent toAdd = new PlannerEvent(task, startTime, startTime + desiredDuration);
                assignments.add(toAdd);
                occupiedTree.add(new OccupiedInterval(toAdd));
                return assignments;
            }

            //  Check if we can push task in between a pair of collision intervals.
            for (IInterval collision : collisions) {
                PlannerEvent possibleEvent = new PlannerEvent(task, startTime, startTime + desiredDuration);
                OccupiedInterval toCheck = new OccupiedInterval(possibleEvent);
                if (toCheck.irBefore(collision)) {
                    // Found free interval before some event/task so we can push here.
                    assignments.add(possibleEvent);
                    occupiedTree.add(toCheck);
                    return assignments;
                }
            }
        }
        return assignments;
    }

    /**
     * Helper function: Returns the tag if it exists in this calendar. Return null if name is null or NO_TAG.
     */
    private PlannerTag safeGetTag(String tagName) {
        if (tagName == null || tagName.equals(PlannerObject.NO_TAG)) {
            return null;
        }
        return tags.get(tagName);
    }

    /**
     * Helper function: Returns true if the given date is within the max range from the start time (30 days).
     */
    private boolean isValidDate(long time) {
        long diffInMillis = time - startTime;
        return diffInMillis >= 0 &&
                TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) <= MAX_DAYS;
    }

    // Inner classes

    /**
     * Iterator the iterates over the non-occupied time intervals in this calendar.
     */
    private class FreeTimeIterator implements Iterator<IInterval> {

        private final Iterator<IInterval> occupiedIt;
        private long startTime, lastEndTime;
        private LongInterval nextOccupied;
        private boolean oneMore;

        /**
         * Constructor for this iterator. Generates free time from the enclosing class' Interval Tree.
         */
        public FreeTimeIterator() {

            occupiedIt = occupiedTree.iterator();
            startTime = PlannerCalendar.this.startTime + spaceBetweenTasks;

            oneMore = true;
            if (occupiedIt.hasNext()) {
                nextOccupied = (LongInterval) occupiedIt.next();
            } else {
                lastEndTime = startTime;
                nextOccupied = null;
            }
        }

        /**
         * Returns true if the iteration has more elements.
         */
        @Override
        public boolean hasNext() {
            return nextOccupied != null || oneMore;
        }

        // On-the-fly interval merging

        /**
         * Helper function: On-the-fly interval merging of free time intervals.
         */
        private LongInterval getNextOccupied() {
            LongInterval previous = nextOccupied;
            if (!occupiedIt.hasNext()) {
                nextOccupied = null;
                lastEndTime = previous.getEnd() + spaceBetweenTasks;
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

        /**
         * Returns the next element in the iteration.
         */
        @Override
        public IInterval next() {
            while (true) {
                if (nextOccupied == null) {
                    if (oneMore) {
                        oneMore = false;
                        return new LongInterval(lastEndTime, PlannerCalendar.this.startTime + TimeUnit.DAYS.toMillis(MAX_DAYS));
                    }
                    return null;
                }

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

    /**
     * Closed interval that contains an event.
     */
    private static class OccupiedInterval extends LongInterval {

        public PlannerEvent event;

        /**
         * Create an interval with no start or end time and no event.
         */
        public OccupiedInterval() {
            super();
            event = null;
        }

        /**
         * Create the closed interval [event.getStartTime(), event.getStartTime()] that point to the given event.
         */
        public OccupiedInterval(PlannerEvent event) throws IllegalTimeInterval, IllegalTimePoint {
            super(event.getStartTime(), event.getStartTime(), false, false);
            this.event = event;
        }

        /**
         * Returns true if both intervals are equal and both events are equal.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            OccupiedInterval that = (OccupiedInterval) o;
            return event.equals(that.event);
        }
    }

}
