package net.planner.exampleapp

import lombok.Getter
import lombok.Setter
import java.util.*

class CalendarInfo @JvmOverloads internal constructor(
    @field:Getter private val id: Int = ALL_CALENDARS_ID,
    @field:Getter private val account: String? = null,
    @field:Getter private val name: String? = null
) {
    @Setter
    @Getter
    private val selected = true
    override fun toString(): String {
        return String.format(Locale.getDefault(), "%s - %s", account, name)
    }

    fun toDebugString(): String {
        return String.format(
            Locale.getDefault(),
            "ID: %d; account: %s; name: %s",
            id,
            account,
            name
        )
    }

    val isAllItem: Boolean
        get() = id == ALL_CALENDARS_ID

    companion object {
        private const val ALL_CALENDARS_ID = -100
    }
}