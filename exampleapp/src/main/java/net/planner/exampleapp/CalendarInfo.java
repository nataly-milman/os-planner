package net.planner.exampleapp;

import androidx.annotation.NonNull;

import java.util.Locale;

public class CalendarInfo {
    private int id;
    private String account;
    private String name;
    private boolean selected = true;

    private static int ALL_CALENDARS_ID = -100;

    CalendarInfo(int id, String account, String name) {
        this.id = id;
        this.account = account;
        this.name = name;
    }

    CalendarInfo() {
        this(ALL_CALENDARS_ID, null, null);
    }

    @NonNull @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s - %s", account, name);
    }

    String toDebugString() {
        return String.format(Locale.getDefault(), "ID: %d; account: %s; name: %s", id, account, name);
    }

    boolean isAllItem() {
        return (id == ALL_CALENDARS_ID);
    }

    public int getId() {
        return id;
    }

    public String getAccount() {
        return account;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
