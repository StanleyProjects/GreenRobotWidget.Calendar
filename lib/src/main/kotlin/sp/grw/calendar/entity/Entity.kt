package sp.grw.calendar.entity

import java.util.Calendar

internal class YearMonth(val year: Int, val month: Int) {
    override fun toString(): String {
        return "{$year/$month}"
    }
}

internal class YearMonthDay(val year: Int, val month: Int, val dayOfMonth: Int) {
    override fun toString(): String {
        return "{$year/$month/$dayOfMonth}"
    }
}

internal class YearWeek(val year: Int, val weekOfYear: Int) {
    override fun toString(): String {
        return "{$year/$weekOfYear}"
    }
}

internal class Payload(
    value: Map<Int, Map<Int, Map<Int, String>>>
) {
    private val value: Map<Int, Map<Int, Map<Int, String>>>

    init {
        val result = mutableMapOf<Int, MutableMap<Int, MutableMap<Int, String>>>()
        value.toList().sortedBy { (year, _) -> year }.forEach { (year, months) ->
            check(year > 0) {"Wrong year $year!"}
            months.toList().sortedBy { (month, _) -> month }.forEach { (month, days) ->
                check(month in 0..11) {"Wrong month $month!"}
                days.toList().sortedBy { (dayOfMonth, _) -> dayOfMonth }.forEach { (dayOfMonth, payload) ->
                    check(dayOfMonth in 1..31) {"Wrong day of month $dayOfMonth!"}
                    if (payload.isNotEmpty()) {
                        result.getOrPut(year) {
                            mutableMapOf()
                        }.getOrPut(month) {
                            mutableMapOf()
                        }[dayOfMonth] = payload
                    }
                }
            }
        }
        this.value = result
    }

    fun getData(year: Int, month: Int, dayOfMonth: Int): String? {
        return value[year]?.get(month)?.get(dayOfMonth)
    }

    fun isEmpty(): Boolean {
        return value.isEmpty()
    }

    fun getYears(): Set<Int> {
        return value.keys
    }

    fun getMonths(year: Int): Set<Int> {
        return value[year]?.keys.orEmpty()
    }

    fun getWeeks(year: Int, firstDayOfWeek: Int): Set<Int> {
        val months = value[year] ?: return emptySet()
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        val (minMonth, minDays) = months.minByOrNull { (k, _) -> k } ?: return emptySet()
        val (minDayOfMonth, _) = minDays.minByOrNull { (k, _) -> k } ?: return emptySet()
        calendar.firstDayOfWeek = firstDayOfWeek
        calendar[Calendar.MONTH] = minMonth
        calendar[Calendar.DAY_OF_MONTH] = minDayOfMonth
        val minWeekOfYear = calendar[Calendar.WEEK_OF_YEAR]
        val (maxMonth, maxDays) = months.maxByOrNull { (k, _) -> k } ?: return emptySet()
        val (maxDayOfMonth, _) = maxDays.maxByOrNull { (k, _) -> k } ?: return emptySet()
        calendar.firstDayOfWeek = firstDayOfWeek
        calendar[Calendar.MONTH] = maxMonth
        calendar[Calendar.DAY_OF_MONTH] = maxDayOfMonth
        val maxWeekOfYear = calendar[Calendar.WEEK_OF_YEAR]
        return (minWeekOfYear..maxWeekOfYear).toSet()
    }

    fun forEachMonths(block: (year: Int, months: Set<Int>) -> Unit) {
        value.forEach { (year, months) ->
            block(year, months.map { (k, _) -> k }.toSet())
        }
    }

    fun forEachWeeks(firstDayOfWeek: Int, block: (year: Int, weeks: Set<Int>) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = firstDayOfWeek
        value.forEach { (year, months) ->
            calendar[Calendar.YEAR] = year
            val weeks = months.map { (month, days) ->
                calendar[Calendar.MONTH] = month
                days.map { (dayOfMonth, _) ->
                    calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                    calendar[Calendar.WEEK_OF_YEAR]
                }
            }.flatten().toSet()
            block(year, weeks)
        }
    }
}

internal fun Payload.isPresent(year: Int, month: Int, dayOfMonth: Int): Boolean {
    val data = getData(year = year, month = month, dayOfMonth = dayOfMonth)
    return !data.isNullOrEmpty()
}

enum class ActiveType {
    ALL,
    PAYLOAD,
    FUTURE,
}
