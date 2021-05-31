package net.planner.planet;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static org.junit.Assert.*;

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
        Assert.assertEquals("Title: New task; Priority: 5/10; Exclusive for this time slot;" +
                " Deadline is Sun May 16 06:00:00 IDT 2021; " +
                "Maximal time of one session (if divided) is 1440; " +
                "Maximal number of divisions (if divided) is 1.", pt.toString());
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
}