package sp.grw.calendar.util

import java.util.Calendar
import sp.grw.calendar.entity.YearMonth
import sp.grw.calendar.entity.YearMonthDay
import sp.grw.calendar.entity.YearWeek

internal object DateUtil {
    const val DAYS_IN_WEEK = 7 // todo

    fun Calendar.toYearMonth(): YearMonth {
        return YearMonth(year = this[Calendar.YEAR], month = this[Calendar.MONTH])
    }

    fun Calendar.toYearMonthDay(): YearMonthDay {
        return YearMonthDay(
            year = this[Calendar.YEAR],
            month = this[Calendar.MONTH],
            dayOfMonth = this[Calendar.DAY_OF_MONTH]
        )
    }

    fun Calendar.toYearWeek(firstDayOfWeek: Int): YearWeek {
        this.firstDayOfWeek = firstDayOfWeek
        return YearWeek(year = this[Calendar.YEAR], weekOfYear = this[Calendar.WEEK_OF_YEAR])
    }

    fun calculateWeeksInMonth(year: Int, month: Int, firstDayOfWeek: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = firstDayOfWeek
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month
        val min = calendar.getActualMinimum(Calendar.WEEK_OF_MONTH)
        val max = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH)
        return max - min
    }

    fun isWeekendDay(firstDayOfWeek: Int, dayOfWeek: Int): Boolean {
        check(dayOfWeek in Calendar.SUNDAY..Calendar.SATURDAY) {
            "Day of week \"$dayOfWeek\" not supported!"
        }
        val weekend = setOf(firstDayOfWeek + 5, firstDayOfWeek + 6).map {
            if (it > 7) it - 7 else it
        }
        return weekend.contains(dayOfWeek)
    }

    fun isToday(
        year: Int,
        month: Int,
        dayOfMonth: Int
    ): Boolean {
        val calendar = Calendar.getInstance()
        return calendar[Calendar.YEAR] == year &&
            calendar[Calendar.MONTH] == month &&
            calendar[Calendar.DAY_OF_MONTH] == dayOfMonth
    }

    fun isSelected(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        dateSelected: YearMonthDay?,
        isAutoSelectToday: Boolean,
        isToday: Boolean = isToday(year = year, month = month, dayOfMonth = dayOfMonth)
    ): Boolean {
        return if (dateSelected == null) isAutoSelectToday && isToday
        else dateSelected.year == year &&
            dateSelected.month == month &&
            dateSelected.dayOfMonth == dayOfMonth
    }
}
