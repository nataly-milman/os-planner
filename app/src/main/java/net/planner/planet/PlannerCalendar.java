package net.planner.planet;

import com.brein.time.exceptions.IllegalTimeInterval;
import com.brein.time.exceptions.IllegalTimePoint;
import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder;
import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.brein.time.timeintervals.intervals.NumberInterval;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PlannerCalendar {

    // Constants
    private static final int MAX_DAYS = 30; // Amount of days in a PlannerCalendar object.
    private static final int SLOT_SIZE = 15; // Minimum number of minutes for an event.

    // Fields
    private long startTime; // This calendar starts from this time (ms) and ends 30 days after it.
    private IntervalTree thisMonth;

    // Constructors
    public PlannerCalendar() {
        init(Calendar.getInstance());
    }

    public PlannerCalendar(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        init(cal);
    }

    public PlannerCalendar(Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        init(cal);
    }

    public PlannerCalendar(Calendar startDate) {
        init((Calendar) startDate.clone());
    }

    private void init(Calendar startDate) {
        // Get time at start of day.
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);
        this.startTime = startDate.getTimeInMillis();

        thisMonth = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.INTEGER).build();
    }

    // Methods
    public Collection<?> getCollisions(long startDate, long endDate) {
        int startSlot = toSlotIndex(startDate);
        int endSlot = toSlotIndex(endDate);

        return thisMonth.find(new IntegerInterval(startSlot, endSlot));
    }

    public boolean isAvailable(long startDate, long endDate) {
        return getCollisions(startDate, endDate).isEmpty();
    }

    public boolean insertEvent(PlannerEvent event) {
        if (!isValidDate(event.getStartTime()) || !isValidDate(event.getEndTime())) {
            return false;
        }

        PlannerInterval toInsert = new PlannerInterval(event);
        return !thisMonth.contains(toInsert) && thisMonth.add(toInsert);
    }

    public boolean forceInsertEvent(PlannerEvent event) {
        if (!isValidDate(event.getStartTime()) || !isValidDate(event.getEndTime())) {
            return false;
        }

        PlannerInterval toInsert = new PlannerInterval(event);
        return thisMonth.add(toInsert);
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
        PlannerInterval toRemove = new PlannerInterval(event);
        // todo current implementation only checks interval but can't actually compare events
        return thisMonth.remove(toRemove);
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

    public long getStartTime() {
        return startTime;
    }

    // Inner classes
    private class PlannerInterval extends NumberInterval<Integer> {

        public PlannerEvent event;

        public PlannerInterval() {
            super();
            this.event = null;
        }

        public PlannerInterval(PlannerEvent event) throws IllegalTimeInterval, IllegalTimePoint {
            super(Integer.class, toSlotIndex(event.getStartTime()), toSlotIndex(event.getEndTime()), false, false);
            this.event = event;
        }
    }

}
