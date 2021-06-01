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
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd H:mm");
        long calendarTestFrom = Objects.requireNonNull(ft.parse("2021-05-13 0:00")).getTime();
        PlannerManager manager = new PlannerManager(false, null, calendarTestFrom);

        // all day event for two days, start and end time wouldn't matter
        long start = Objects.requireNonNull(ft.parse("2021-05-13 14:00")).getTime();
        long end = Objects.requireNonNull(ft.parse("2021-05-14 12:00")).getTime();
        manager.addEvent("trip", start, end, true);

        manager.addEvent("6 hours of suffering", start, end);
        // can do something simultaneously
        start = Objects.requireNonNull(ft.parse("2021-05-16 06:00")).getTime();
        end = Objects.requireNonNull(ft.parse("2021-05-16 12:00")).getTime();
        manager.addEvent("6 hours of suffering", start, end);

        // 3 free hours
        // cannot do anything simultaneously
        start = Objects.requireNonNull(ft.parse("2021-05-16 15:00")).getTime();
        end = Objects.requireNonNull(ft.parse("2021-05-16 18:30")).getTime();
        manager.addEvent("lecture", start, end,  false, false);

        List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> preferredTI =
                List.of(new Pair<>(new Pair<>(17, 0), new Pair<>(22, 30)));
        manager.addOrEditTag("sport", null, preferredTI, 8);

        long deadline = Objects.requireNonNull(ft.parse("2021-05-16 23:30")).getTime();
        // unknown tag = exception
        //manager.addTask("Yoga", deadline, 45,  "some test gibberish", 5);
        manager.addTask("Yoga", deadline, 45,  "sport", 5);

    }

}


