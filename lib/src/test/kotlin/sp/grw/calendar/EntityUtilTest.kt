package sp.grw.calendar

import java.util.Calendar
import org.junit.Test
import sp.grw.calendar.entity.Payload
import org.junit.Assert.assertTrue

class EntityUtilTest {
    @Test
    fun forEachWeeks2001Test() {
        val firstDayOfWeek = Calendar.MONDAY
        val year = 2001
        val source = mapOf(
            year to mapOf(
                Calendar.JANUARY to mapOf(
                    1 to "$year/${Calendar.JANUARY}/1"
                ),
                Calendar.FEBRUARY to mapOf(
                    12 to "$year/${Calendar.FEBRUARY}/12"
                ),
                Calendar.JULY to mapOf(
                    29 to "$year/${Calendar.JULY}/29"
                ),
                Calendar.OCTOBER to mapOf(
                    8 to "$year/${Calendar.JULY}/8"
                ),
                Calendar.DECEMBER to mapOf(
                    31 to "$year/${Calendar.DECEMBER}/31"
                )
            )
        )
        val payload = Payload(source)
        val result = mutableMapOf<Int, Set<Int>>()
        payload.forEachWeeks(firstDayOfWeek = firstDayOfWeek) { y, weeks ->
            result[y] = weeks
        }
        assertTrue("$result", result == mapOf(year to setOf(1, 7, 30, 41, 53)))
    }

    @Test
    fun forEachWeeks2021Test() {
        val firstDayOfWeek = Calendar.MONDAY
        val year = 2021
        val source = mapOf(
            year to mapOf(
                Calendar.JANUARY to mapOf(
                    1 to "$year/${Calendar.JANUARY}/1"
                ),
                Calendar.DECEMBER to mapOf(
                    31 to "$year/${Calendar.DECEMBER}/31"
                )
            )
        )
        val payload = Payload(source)
        val result = mutableMapOf<Int, Set<Int>>()
        payload.forEachWeeks(firstDayOfWeek = firstDayOfWeek) { y, weeks ->
            result[y] = weeks
        }
        assertTrue(result == mapOf(year to setOf(1, 53)))
    }

    @Test
    fun forEachWeeks2020Test() {
        val firstDayOfWeek = Calendar.MONDAY
        val year = 2020
        val source = mapOf(
            year to mapOf(
                Calendar.JANUARY to mapOf(
                    1 to "$year/${Calendar.JANUARY}/1"
                ),
                Calendar.DECEMBER to mapOf(
                    31 to "$year/${Calendar.DECEMBER}/31"
                )
            )
        )
        val payload = Payload(source)
        val result = mutableMapOf<Int, Set<Int>>()
        payload.forEachWeeks(firstDayOfWeek = firstDayOfWeek) { y, weeks ->
            result[y] = weeks
        }
        assertTrue(result == mapOf(year to setOf(1, 53)))
    }
}
