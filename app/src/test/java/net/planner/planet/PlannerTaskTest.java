package net.planner.planet;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class PlannerTaskTest {
    @Test
    public void taskCreation() {
        SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yy h:mm");
        long deadline = 0;
        try {
            deadline = Objects.requireNonNull(ft.parse("16/05/21 6:00")).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        PlannerTask pt = new PlannerTask("New task", deadline, 30);
        Assert.assertNotNull(pt);
        Assert.assertEquals("Title: New task; Tagged: NoTag; Exclusive for this time slot; " +
                            "Priority: 5/10;" + " Deadline is Sun May 16 06:00:00 IDT 2021; " +
                            "Maximal time of one session (if divided) is 60; " +
                            "Maximal number of divisions (if divided) is 1; " +
                            "Expected duration of the task in minutes is: 30.", pt.toString());
        // priority
        Assert.assertFalse(pt.setPriority(0));
        Assert.assertFalse(pt.getPriority() < 1);
    }

    @Test
    public void taskAccess() {
        long date1 = 1021950123449L;
        long date2 = 1621956543086L;
        PlannerTask pt = new PlannerTask("New task", date2, 60);
        Assert.assertNotNull(pt);
        PlannerEvent pe = new PlannerEvent("New event", date1, date2);
        Assert.assertNotNull(pe);
        //PlannerObject po = new PlannerObject("Isn't reachable");
    }

    @Test
    public void testValidity() {
        Assert.assertFalse(PlannerTask.isValid(-20, -1, -1L, -1, -1, -1));
        Assert.assertFalse(PlannerTask.isValid(15, -1, -1L, -1, -1, -1));
        Assert.assertFalse(PlannerTask.isValid(15, 4, -1L, -1, -1, -1));
        Assert.assertFalse(PlannerTask.isValid(15, 4, 1021950123449L, -1, -1, -1));
        Assert.assertFalse(PlannerTask.isValid(15, 4, 1021950123449L, 180, -1, -1));
        Assert.assertFalse(PlannerTask.isValid(15, 4, 1021950123449L, 180, 10, -1));
        Assert.assertFalse(PlannerTask.isValid(15, 4, 1021950123449L, 180, 30, -1));
        // impossible to get this duration even though the values by themselves are valid:
        Assert.assertFalse(PlannerTask.isValid(15, 4, 1021950123449L, 180, 30, 1));

        Assert.assertTrue(PlannerTask.isValid(15, 4, 1021950123449L, 180, 30, 10));
    }
}