package net.planner.flow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kotlin.Pair;

import net.planner.planet.PlannerManager;
import net.planner.planet.PlannerTask;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
        // unknown tag is acceptable now
        manager.addTask("Yoga", deadline, 45,  "weird new tag", 5);
        manager.addTask("Yoga", deadline, 45,  "sport", 5);

    }


    @Test
    public void noGoogleCalendarGroupOfTasks() throws ParseException {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd H:mm");
        long calendarTestFrom = Objects.requireNonNull(ft.parse("2021-05-13 0:00")).getTime();
        PlannerManager manager = new PlannerManager(false, null, calendarTestFrom);


        manager.addOrEditTag("sport", null, null, 6);
        manager.addOrEditTag("outside", null, null, 4);
        manager.addOrEditTag("school", null, null, 9);

        // all day event for two days, start and end time wouldn't matter
        LinkedList<PlannerTask> tasks = new LinkedList<>();
        long deadline = Objects.requireNonNull(ft.parse("2021-05-13 14:00")).getTime();
        tasks.add(manager.createTask("trip", deadline, 300, "outside", 4));
        deadline = Objects.requireNonNull(ft.parse("2021-05-13 23:59")).getTime();
        tasks.add(manager.createTask("hw infi", deadline, 120, "school", 7));
        tasks.add(manager.createTask("hw oop", deadline, 120, "school", 9));
        deadline = Objects.requireNonNull(ft.parse("2021-05-16 06:00")).getTime();
        tasks.add(manager.createTask("trip part 2", deadline, 150, "outside", 5));
        tasks.add(manager.createTask("sport", deadline, 45, "sport", 8));

        manager.addTasksList(tasks);

    }
}


