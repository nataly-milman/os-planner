package net.planner.planet;

import android.util.Pair;

import com.brein.time.timeintervals.intervals.IInterval;
import com.brein.time.timeintervals.intervals.LongInterval;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.DoubleAccumulator;

import static org.junit.Assert.*;

public class PlannerTagTest {

    @Test
    public void tagCreation() {
        PlannerTag tag = new PlannerTag("work");
        Assert.assertNotNull(tag);
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd h:mm");
        long date1 = 0, date2 = 0;
        try {
            date1 = Objects.requireNonNull(ft.parse("2021-05-16 6:00")).getTime();
            date2 = Objects.requireNonNull(ft.parse("2021-05-16 18:00")).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tag.addForbiddenTimeInterval(date1, date2);
        List<IInterval> expected = new LinkedList<>();
        expected.add(new LongInterval(date1, date2));

        List<IInterval> obtained = tag.getForbiddenTimeIntervals();

        Assert.assertEquals(expected.size(), obtained.size());

        boolean found;
        for (int i = 0; i < obtained.size(); i++) {
            IInterval interval = obtained.get(i);
            found = false;
            for (int j = 0; j < expected.size(); j++) {
                if (interval.equals(expected.get(j))) {
                    found = true;
                }
            }
            Assert.assertTrue(found);
        }
    }

    @Test
    public void tagTimeDefinition() {
        PlannerTag tag = new PlannerTag("work");
        Assert.assertNotNull(tag);
        SimpleDateFormat ft = new SimpleDateFormat("h:mm");
        long date1 = 0, date2 = 0;
        try {
            date1 = Objects.requireNonNull(ft.parse("6:00")).getTime();
            date2 = Objects.requireNonNull(ft.parse("18:00")).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tag.addForbiddenTimeInterval(date1, date2);
    }
}