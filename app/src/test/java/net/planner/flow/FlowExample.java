package net.planner.flow;

import kotlin.Pair;

import net.planner.planet.PlannerManager;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

public class FlowExample {

    @Test
    public void noGoogleCalendarFlow() throws ParseException {
        PlannerManager manager = new PlannerManager(false, null);

        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd h:mm");
        // can do something simultaneously
        long start = Objects.requireNonNull(ft.parse("2021-05-16 06:00")).getTime();
        long end = Objects.requireNonNull(ft.parse("2021-05-16 12:00")).getTime();
        manager.addEvent(start, end, "6 hours of suffering");

        // 3 free hours
        // cannot do anything simultaneously
        start = Objects.requireNonNull(ft.parse("2021-05-16 15:00")).getTime();
        end = Objects.requireNonNull(ft.parse("2021-05-16 18:30")).getTime();
        manager.addEvent(start, end, "lecture", false, false);

        List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> preferredTI =
                List.of(new Pair<>(new Pair<>(17, 0), new Pair<>(22, 30)));
        manager.addOrEditTag("sport", null, preferredTI, 8);


    }

}