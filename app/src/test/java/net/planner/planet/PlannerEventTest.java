package net.planner.planet;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlannerEventTest {

    @Test
    public void eventCreation(){
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd h:mm");
        Date date1 = null, date2 = null;
        try {
            date1 = ft.parse("2021-05-16 6:00");
            date2 = ft.parse("2021-05-16 18:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        PlannerEvent pe = new PlannerEvent("New event", date1, date2);
        Assert.assertNotNull(pe);
        Assert.assertEquals("Title: New event; Starts at Sun May 16 06:00:00 IDT 2021; " +
                "Ends at Sun May 16 18:00:00 IDT 2021; Priority: 5/10.", pe.toString());
    }

    @Test
    public void testBasicValues(){
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd h:mm");
        Date date1 = null, date2 = null, date3 = null;
        try {
            date1 = ft.parse("2021-05-16 6:00");
            date2 = ft.parse("2021-05-16 18:00");
            date3 = ft.parse("2021-05-16 5:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        PlannerEvent pe = new PlannerEvent("New event", date1, date2);
        pe.setTitle(null);
        Assert.assertNotNull(pe.getTitle());
//        pe.setStartTime(null);
//        Assert.assertNotNull(pe.getStartTime());
        // check end
        pe.setEndTime(date3); // set end before start
        Assert.assertFalse(pe.getEndTime().before(pe.getStartTime()));  // NOT end before start
        // reset to a valid config
        pe.setStartTime(date3);
        pe.setEndTime(date1); // start 5:00, end 6:00
        Assert.assertEquals(pe.getStartTime(), date3);  // just in case
        Assert.assertEquals(pe.getEndTime(), date1);  // just in case
        // check start
        pe.setStartTime(date2); // set start after end
        Assert.assertFalse(pe.getEndTime().before(pe.getStartTime()));  // NOT end before start
        // reminder
        pe.setReminder(-10);
        Assert.assertEquals(-1, pe.getReminder());
        // priority
        pe.setPriority(0);
        Assert.assertFalse(pe.getPriority() < 1);
    }
}