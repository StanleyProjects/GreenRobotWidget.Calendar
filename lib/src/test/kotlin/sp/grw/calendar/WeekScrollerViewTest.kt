package sp.grw.calendar

import java.util.Calendar
import java.util.TimeZone
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import sp.grw.calendar.WeekScrollerView.Companion.offsetAfter
import sp.grw.calendar.WeekScrollerView.Companion.offsetBefore
import sp.grw.calendar.entity.Payload

class WeekScrollerViewTest {
    @Test(timeout = 10_000)
    fun onlyWeeksWithDaysEmptyTest() {
        val payload = Payload(emptyMap())
        val result = WeekScrollerView.onlyWeeksWithDays(
            payload = payload,
            firstDayOfWeek = Calendar.MONDAY,
            timeZone = TimeZone.getTimeZone("UTC"),
            isEmptyTodayWeekSkipped = true,
        )
        assertTrue(result.isEmpty())
    }

    @Test(timeout = 10_000)
    fun onlyWeeksWithDaysTest() {
        val year = 2021
        val payload = Payload(
            mapOf(
                year to mapOf(
                    Calendar.JANUARY to mapOf(
                        1 to "$year/${Calendar.JANUARY}/1"
                    ),
                    Calendar.MARCH to mapOf(
                        15 to "$year/${Calendar.MARCH}/15"
                    ),
                    Calendar.JUNE to mapOf(
                        20 to "$year/${Calendar.JUNE}/20"
                    ),
                    Calendar.DECEMBER to mapOf(
                        31 to "$year/${Calendar.DECEMBER}/31"
                    )
                )
            )
        )
        val result = WeekScrollerView.onlyWeeksWithDays(
            payload = payload,
            firstDayOfWeek = Calendar.MONDAY,
            timeZone = TimeZone.getTimeZone("UTC"),
            isEmptyTodayWeekSkipped = true,
        )
        assertTrue("$result", result == mapOf(year to setOf(1, 12, 25, 53)))
    }

    @Test(timeout = 10_000)
    fun getWeeksEmptyTest() {
        val payload = Payload(emptyMap())
        val result = WeekScrollerView.getWeeks(
            payload = payload,
            firstDayOfWeek = Calendar.MONDAY,
            timeZone = TimeZone.getTimeZone("UTC"),
            monthOffsetBefore = 0,
            monthOffsetAfter = 0,
            isEmptyWeeksSkipped = true,
            isEmptyTodayWeekSkipped = true
        )
        assertTrue(result.isEmpty())
    }

    @Test(timeout = 10_000)
    fun getWeeksFirstTest() {
        val year = 2000
        val payload = Payload(
            mapOf(year to mapOf(Calendar.JANUARY to mapOf(1 to "test")))
        )
        val result = WeekScrollerView.getWeeks(
            payload = payload,
            firstDayOfWeek = Calendar.MONDAY,
            timeZone = TimeZone.getTimeZone("UTC"),
            monthOffsetBefore = 0,
            monthOffsetAfter = 0,
            isEmptyWeeksSkipped = true,
            isEmptyTodayWeekSkipped = true
        )
        assertTrue(result.keys.size == 1)
        assertEquals(year, result.keys.single())
        assertTrue(result[year]!!.size == 1)
        assertEquals(1, result[year]!!.single())
    }

    @Test(timeout = 10_000)
    fun getWeeksTest() {
        val year = 2021
        val payload = Payload(
            mapOf(
                year to mapOf(
                    Calendar.JANUARY to mapOf(
                        1 to "$year/${Calendar.JANUARY}/1"
                    ),
                    Calendar.DECEMBER to mapOf(
                        26 to "$year/${Calendar.DECEMBER}/26"
//                        31 to "$year/${Calendar.DECEMBER}/31"
                    )
                )
            )
        )
        val result = WeekScrollerView.getWeeks(
            payload = payload,
            firstDayOfWeek = Calendar.MONDAY,
            timeZone = TimeZone.getTimeZone("UTC"),
            monthOffsetBefore = 2,
            monthOffsetAfter = 2,
            isEmptyWeeksSkipped = true,
            isEmptyTodayWeekSkipped = true
        )
        assertEquals("$result", 3, result.keys.size)
        val y0 = assertNotNull(result[2020])
        assertEquals("$result", setOf(49, 50, 51, 52, 53), y0)
        val y1 = assertNotNull(result[2021])
        assertEquals("$result", setOf(1, 52, 53), y1)
        val y2 = assertNotNull(result[2022])
        assertEquals("$result", setOf(1, 2, 3, 4, 5, 6), y2)
    }

    @Test(timeout = 10_000)
    fun offsetBeforeTest() {
        val firstDayOfWeek = Calendar.MONDAY
        val timeZone = TimeZone.getTimeZone("UTC")
        val minimalDaysInFirstWeek = 1
        val target = Calendar.getInstance().also {
            it.timeZone = timeZone
            it.firstDayOfWeek = firstDayOfWeek
            it.minimalDaysInFirstWeek = minimalDaysInFirstWeek
            it[Calendar.YEAR] = 2020
            it[Calendar.MONTH] = Calendar.DECEMBER
        }
        val offset = mutableMapOf<Int, MutableSet<Int>>()
        offset.offsetBefore(
            firstDayOfWeek = firstDayOfWeek,
            timeZone = timeZone,
            year = 2021,
            week = 1,
            target = target
        )
        assertEquals("$offset", mapOf(2020 to setOf(53, 52, 51, 50, 49)), offset)
    }

    @Test(timeout = 10_000)
    fun offsetBeforeLongTest() {
        val firstDayOfWeek = Calendar.MONDAY
        val timeZone = TimeZone.getTimeZone("UTC")
        val minimalDaysInFirstWeek = 1
        val target = Calendar.getInstance().also {
            it.timeZone = timeZone
            it.firstDayOfWeek = firstDayOfWeek
            it.minimalDaysInFirstWeek = minimalDaysInFirstWeek
            it[Calendar.YEAR] = 2019
            it[Calendar.MONTH] = Calendar.DECEMBER
        }
        val offset = mutableMapOf<Int, MutableSet<Int>>()
        offset.offsetBefore(
            firstDayOfWeek = firstDayOfWeek,
            timeZone = timeZone,
            year = 2021,
            week = 2,
            target = target
        )
        val expected = mapOf(
            2019 to setOf(53, 52, 51, 50, 49, 48),
            2020 to (53 downTo 1).toSet(),
            2021 to setOf(1),
        )
        assertEquals("$offset", expected.keys.size, offset.keys.size)
        expected.keys.forEach {
            assertEquals("$offset year: $it", expected[it], offset[it])
        }
    }

    @Test(timeout = 10_000)
    fun offsetAfterTest() {
        val firstDayOfWeek = Calendar.MONDAY
        val timeZone = TimeZone.getTimeZone("UTC")
        val minimalDaysInFirstWeek = 1
        val target = Calendar.getInstance().also {
            it.timeZone = timeZone
            it.firstDayOfWeek = firstDayOfWeek
            it.minimalDaysInFirstWeek = minimalDaysInFirstWeek
            it[Calendar.YEAR] = 2021
            it[Calendar.MONTH] = Calendar.JANUARY
        }
        val offset = mutableMapOf<Int, MutableSet<Int>>()
        offset.offsetAfter(
            firstDayOfWeek = firstDayOfWeek,
            timeZone = timeZone,
            year = 2020,
            week = 53,
            target = target
        )
        assertEquals("$offset", mapOf(2021 to setOf(1, 2, 3, 4, 5)), offset)
    }

    @Test(timeout = 10_000)
    fun offsetAfterLongTest() {
        val firstDayOfWeek = Calendar.MONDAY
        val timeZone = TimeZone.getTimeZone("UTC")
        val minimalDaysInFirstWeek = 1
        val target = Calendar.getInstance().also {
            it.timeZone = timeZone
            it.firstDayOfWeek = firstDayOfWeek
            it.minimalDaysInFirstWeek = minimalDaysInFirstWeek
            it[Calendar.YEAR] = 2022
            it[Calendar.MONTH] = Calendar.JANUARY
        }
        val offset = mutableMapOf<Int, MutableSet<Int>>()
        offset.offsetAfter(
            firstDayOfWeek = firstDayOfWeek,
            timeZone = timeZone,
            year = 2020,
            week = 52,
            target = target
        )
        assertEquals(
            "$offset",
            mapOf(
                2020 to setOf(53),
                2021 to (1..53).toSet(),
                2022 to setOf(1, 2, 3, 4, 5, 6),
            ),
            offset
        )
    }
}
