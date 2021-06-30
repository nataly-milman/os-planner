package net.planner.planet;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class PlannerEventTest {

    @Test
    public void eventCreation() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd h:mm");
        Date date1 = null, date2 = null;
        try {
            date1 = ft.parse("2021-05-16 6:00");
            date2 = ft.parse("2021-05-16 18:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date1 != null;
        assert date2 != null;
        PlannerEvent pe = new PlannerEvent("New event", date1.getTime(), date2.getTime());
        Assert.assertNotNull(pe);
        Assert.assertEquals("Title: New event; Tagged: NoTag; Exclusive for this time slot;" +
                        " Starts at Sun May 16 06:00:00 IDT 2021; Ends at Sun May 16 18:00:00 IDT 2021.",
                pe.toString());
    }

    @Test
    public void testBasicValues() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd h:mm");
        long date1 = 0, date2 = 0, date3 = 0;
        try {
            date1 = Objects.requireNonNull(ft.parse("2021-05-16 06:00")).getTime();
            date2 = Objects.requireNonNull(ft.parse("2021-05-16 18:00")).getTime();
            date3 = Objects.requireNonNull(ft.parse("2021-05-16 05:00")).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        PlannerEvent pe = new PlannerEvent("New event", date1, date2);
        pe.setTitle(null);
        Assert.assertNotNull(pe.getTitle());
        // check end
        Assert.assertFalse(pe.setEndTime(date3)); // set end before start
        Assert.assertFalse(pe.getEndTime() < pe.getStartTime());  // NOT end before start
        // reset to a valid config
        pe.setStartTime(date3);
        pe.setEndTime(date1); // start 5:00, end 6:00
        Assert.assertEquals(pe.getStartTime(), date3);  // just in case
        Assert.assertEquals(pe.getEndTime(), date1);  // just in case
        // check start
        pe.setStartTime(date2); // set start after end
        Assert.assertFalse(pe.getEndTime() < pe.getStartTime());  // NOT end before start
        // reminder
        pe.setReminder(-10);
        Assert.assertEquals(-1, pe.getReminder());
    }

    @Test
    public void moveTime() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd h:mm");
        long date1 = 0, date2 = 0, newStart = 0, expectedEnd = 0;
        try {
            date1 = Objects.requireNonNull(ft.parse("2021-05-16 6:00")).getTime();
            date2 = Objects.requireNonNull(ft.parse("2021-05-16 7:00")).getTime();
            newStart = Objects.requireNonNull(ft.parse("2021-05-16 15:00")).getTime();
            expectedEnd = Objects.requireNonNull(ft.parse("2021-05-16 16:00")).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        PlannerEvent pe = new PlannerEvent("Test time change", date1, date2);
        pe.setStartTime(newStart);
        Assert.assertEquals(expectedEnd, pe.getEndTime());  // change start = move event

        Assert.assertFalse(pe.setEndTime(date2));
        Assert.assertNotEquals(pe.getEndTime(), date2); //can't set end before start
    }


    @Test
    public void testValidity() {
        long date1 = 1021950123449L;
        long date2 = 1621956543086L;
        Assert.assertFalse(PlannerEvent.isValid(-10, date2, date2));
        Assert.assertFalse(PlannerEvent.isValid(-1, date2, date1));
        Assert.assertTrue(PlannerEvent.isValid(-1, date2, date2));
        Assert.assertTrue(PlannerEvent.isValid(-1, date1, date2));
    }

    @Test
    public void testAllDay() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd h:mm");
        try {
            long date1 = Objects.requireNonNull(ft.parse("2021-05-16 6:00")).getTime();
            long date2 = Objects.requireNonNull(ft.parse("2021-05-16 7:00")).getTime();
            long sod = Objects.requireNonNull(ft.parse("2021-05-16 00:00")).getTime();
            long eod = Objects.requireNonNull(ft.parse("2021-05-17 00:00")).getTime();

            PlannerEvent pe = new PlannerEvent("Test all day settings", date1, date2);
            pe.setAllDay(true);
            Assert.assertEquals(new Date(sod), new Date(pe.getStartTime()));  // starts 17th
            Assert.assertEquals(new Date(eod), new Date(pe.getEndTime()));  // ends 17th

            Assert.assertFalse(pe.setEndTime(date2));
            Assert.assertFalse(pe.setStartTime(date1));

            date2 = Objects.requireNonNull(ft.parse("2021-05-16 7:40")).getTime();
            pe = new PlannerEvent("Test all day settings", date1, date2);
            pe.setAllDay(true);
            Assert.assertEquals(new Date(sod), new Date(pe.getStartTime()));  // starts 17th
            Assert.assertEquals(new Date(eod), new Date(pe.getEndTime()));  // ends 17th

            date1 = Objects.requireNonNull(ft.parse("2021-05-16 00:00")).getTime();
            date2 = Objects.requireNonNull(ft.parse("2021-05-16 00:00")).getTime();
            pe = new PlannerEvent("Test all day settings", date1, date2);
            pe.setAllDay(true);
            Assert.assertEquals(new Date(sod), new Date(pe.getStartTime()));  // starts 16th
            Assert.assertEquals(new Date(eod), new Date(pe.getEndTime()));  // ends 17th

            date2 = Objects.requireNonNull(ft.parse("2021-05-17 7:40")).getTime();
            eod = Objects.requireNonNull(ft.parse("2021-05-18 00:00")).getTime();
            pe = new PlannerEvent("Test all day settings", date1, date2);
            pe.setAllDay(true);
            Assert.assertEquals(new Date(sod), new Date(pe.getStartTime()));  // starts 17th
            Assert.assertEquals(new Date(eod), new Date(pe.getEndTime()));  // ends 18th

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}