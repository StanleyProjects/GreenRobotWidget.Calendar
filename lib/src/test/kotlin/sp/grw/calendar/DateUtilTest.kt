package sp.grw.calendar

import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test
import sp.grw.calendar.util.DateUtil.getWeekOfYear

class DateUtilTest {
    @Test
    fun getWeekOfYearTest() {
        val firstDayOfWeek = Calendar.MONDAY
        val timeZone = TimeZone.getTimeZone("UTC")
        val minimalDaysInFirstWeek = 1
        val calendar = Calendar.getInstance().also {
            it.timeZone = timeZone
            it.firstDayOfWeek = firstDayOfWeek
            it.minimalDaysInFirstWeek = minimalDaysInFirstWeek
        }
        mapOf(
            Triple(2001, Calendar.JANUARY, 1) to 1,
            Triple(2001, Calendar.DECEMBER, 31) to 53,
            Triple(2020, Calendar.JANUARY, 1) to 1,
            Triple(2020, Calendar.JANUARY, 5) to 1,
            Triple(2020, Calendar.JANUARY, 6) to 2,
            Triple(2020, Calendar.JANUARY, 12) to 2,
            Triple(2020, Calendar.DECEMBER, 21) to 52,
            Triple(2020, Calendar.DECEMBER, 27) to 52,
            Triple(2020, Calendar.DECEMBER, 31) to 53,
            Triple(2021, Calendar.JANUARY, 1) to 1,
            Triple(2021, Calendar.JANUARY, 3) to 1,
            Triple(2021, Calendar.JANUARY, 4) to 2,
            Triple(2021, Calendar.JANUARY, 10) to 2,
            Triple(2021, Calendar.OCTOBER, 25) to 44,
            Triple(2021, Calendar.OCTOBER, 31) to 44,
            Triple(2021, Calendar.NOVEMBER, 1) to 45,
            Triple(2021, Calendar.NOVEMBER, 7) to 45,
            Triple(2021, Calendar.NOVEMBER, 8) to 46,
            Triple(2021, Calendar.DECEMBER, 20) to 52,
            Triple(2021, Calendar.DECEMBER, 26) to 52,
            Triple(2021, Calendar.DECEMBER, 27) to 53,
            Triple(2021, Calendar.DECEMBER, 31) to 53,
            Triple(2022, Calendar.JANUARY, 1) to 1,
            Triple(2022, Calendar.JANUARY, 2) to 1,
            Triple(2022, Calendar.JANUARY, 3) to 2,
            Triple(2022, Calendar.JANUARY, 9) to 2
        ).forEach { (y, m, d), expected ->
            calendar.also {
                it[Calendar.YEAR] = y
                it[Calendar.MONTH] = m
                it[Calendar.DAY_OF_MONTH] = d
            }
            assertEquals("$y/$m/$d", expected, calendar.getWeekOfYear(firstDayOfWeek = firstDayOfWeek))
        }
    }
}
