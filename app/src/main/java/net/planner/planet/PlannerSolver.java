package net.planner.planet;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class PlannerSolver {

    private static LinkedList<PlannerTask> sortTasks(List<PlannerTask> tasks, PlannerCalendar calendar){
        LinkedList<PlannerTask> sortedTasks = new LinkedList<>();
        TreeMap<Integer, LinkedList<PlannerTask>> tasksByTagPriority = new TreeMap<>();
        int priority;
        for (PlannerTask task : tasks){
            priority = calendar.getTag(task.getTagName()).getPriority();
            if (tasksByTagPriority.containsKey(priority)){
                Objects.requireNonNull(tasksByTagPriority.get(priority)).add(task);
            } else {
                LinkedList<PlannerTask> newGroup = new LinkedList<>();
                newGroup.add(task);
                tasksByTagPriority.put(priority, newGroup);
            }
        }

        for (Integer name: tasksByTagPriority.descendingKeySet()) {
            String key = name.toString();
            LinkedList<PlannerTask> value = tasksByTagPriority.get(name);
            if (value == null) continue;
            // high to low task priority in this tag priority group
            Collections.sort(Objects.requireNonNull(tasksByTagPriority.get(name)),
                    (o1, o2) -> -(o1.getPriority() - o2.getPriority()));
            sortedTasks.addAll(value);
        }
        return sortedTasks;
    }

    public static void addTasks(List<PlannerTask> tasks, PlannerCalendar calendar){
        for (PlannerTask task : sortTasks(tasks, calendar)){
            calendar.insertTask(task);
        }
    }
}
