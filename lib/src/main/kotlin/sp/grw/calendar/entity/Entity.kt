package sp.grw.calendar.entity

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

    fun forEachMonths(block: (year: Int, months: Set<Int>) -> Unit) {
        value.forEach { (year, months) ->
            block(year, months.map { (k, _) -> k }.toSet())
        }
    }
}
