package sp.grw.calendar

import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test
import sp.grw.calendar.util.DateUtil
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

    @Test
    fun calculateWeeksInMonthTest() {
        val firstDayOfWeek = Calendar.MONDAY
        mapOf(
            (2019 to Calendar.JANUARY) to 5,
            (2019 to Calendar.FEBRUARY) to 5,
            (2019 to Calendar.MAY) to 5,
            (2019 to Calendar.SEPTEMBER) to 6,
            (2019 to Calendar.DECEMBER) to 6,
            (2020 to Calendar.JANUARY) to 5,
            (2020 to Calendar.FEBRUARY) to 5,
            (2020 to Calendar.MARCH) to 6,
            (2020 to Calendar.APRIL) to 5,
            (2021 to Calendar.MARCH) to 5,
            (2021 to Calendar.FEBRUARY) to 4,
            (2021 to Calendar.JANUARY) to 5,
            (2022 to Calendar.JANUARY) to 6,
            (2022 to Calendar.FEBRUARY) to 5,
            (2022 to Calendar.MAY) to 6,
            (2022 to Calendar.AUGUST) to 5,
            (2022 to Calendar.SEPTEMBER) to 5,
            (2022 to Calendar.OCTOBER) to 6
        ).forEach { (y, m), expected ->
            val actual = DateUtil.calculateWeeksInMonth(
                year = y,
                month = m,
                firstDayOfWeek = firstDayOfWeek
            )
            assertEquals("$y/$m", expected, actual)
        }
    }

    @Test
    fun getDayOfWeekAfterTest() {
        setOf(
            Triple(Calendar.TUESDAY, 1, Calendar.WEDNESDAY),
            Triple(Calendar.TUESDAY, 0, Calendar.TUESDAY),
            Triple(Calendar.MONDAY, 14, Calendar.MONDAY),
            Triple(Calendar.MONDAY, 13, Calendar.SUNDAY),
            Triple(Calendar.MONDAY, 12, Calendar.SATURDAY),
            Triple(Calendar.MONDAY, 11, Calendar.FRIDAY),
            Triple(Calendar.MONDAY, 10, Calendar.THURSDAY),
            Triple(Calendar.MONDAY, 9, Calendar.WEDNESDAY),
            Triple(Calendar.MONDAY, 8, Calendar.TUESDAY),
            Triple(Calendar.MONDAY, 7, Calendar.MONDAY),
            Triple(Calendar.MONDAY, 6, Calendar.SUNDAY),
            Triple(Calendar.MONDAY, 5, Calendar.SATURDAY),
            Triple(Calendar.MONDAY, 4, Calendar.FRIDAY),
            Triple(Calendar.MONDAY, 3, Calendar.THURSDAY),
            Triple(Calendar.MONDAY, 2, Calendar.WEDNESDAY),
            Triple(Calendar.MONDAY, 1, Calendar.TUESDAY),
            Triple(Calendar.MONDAY, 0, Calendar.MONDAY)
        ).forEach { (dayOfWeek, after, expected) ->
            val actual = DateUtil.getDayOfWeekAfter(dayOfWeek = dayOfWeek, after = after)
            assertEquals("$dayOfWeek/$after", expected, actual)
        }
    }

    @Test
    fun isWeekendDayTest() {
        Calendar.MONDAY.also { firstDayOfWeek ->
            setOf(
                Calendar.MONDAY to false,
                Calendar.TUESDAY to false,
                Calendar.WEDNESDAY to false,
                Calendar.THURSDAY to false,
                Calendar.FRIDAY to false,
                Calendar.SATURDAY to true,
                Calendar.SUNDAY to true
            ).forEach { (dayOfWeek, expected) ->
                val actual = DateUtil.isWeekendDay(firstDayOfWeek = firstDayOfWeek, dayOfWeek = dayOfWeek)
                assertEquals("$dayOfWeek", expected, actual)
            }
        }
        Calendar.TUESDAY.also { firstDayOfWeek ->
            setOf(
                Calendar.MONDAY to true,
                Calendar.TUESDAY to false,
                Calendar.WEDNESDAY to false,
                Calendar.THURSDAY to false,
                Calendar.FRIDAY to false,
                Calendar.SATURDAY to false,
                Calendar.SUNDAY to true
            ).forEach { (dayOfWeek, expected) ->
                val actual = DateUtil.isWeekendDay(firstDayOfWeek = firstDayOfWeek, dayOfWeek = dayOfWeek)
                assertEquals("$dayOfWeek", expected, actual)
            }
        }
        Calendar.WEDNESDAY.also { firstDayOfWeek ->
            setOf(
                Calendar.MONDAY to true,
                Calendar.TUESDAY to true,
                Calendar.WEDNESDAY to false,
                Calendar.THURSDAY to false,
                Calendar.FRIDAY to false,
                Calendar.SATURDAY to false,
                Calendar.SUNDAY to false
            ).forEach { (dayOfWeek, expected) ->
                val actual = DateUtil.isWeekendDay(firstDayOfWeek = firstDayOfWeek, dayOfWeek = dayOfWeek)
                assertEquals("$dayOfWeek", expected, actual)
            }
        }
    }
}
