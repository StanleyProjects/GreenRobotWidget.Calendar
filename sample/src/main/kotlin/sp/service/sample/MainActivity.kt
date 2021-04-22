package sp.service.sample

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import java.util.Calendar
import sp.grw.calendar.MonthScrollerView
import sp.grw.calendar.ScheduleView
import sp.grw.calendar.WeekScrollerView

class MainActivity : Activity() {
	private class Event(val startTime: Long, val endTime: Long, val type: String)
	private val events = mapOf(
    	2021 to mapOf(
    		Calendar.APRIL to mapOf(
                2 to "april2",
                3 to "april3",
                4 to "april4"
            ),
    		Calendar.JUNE to mapOf(
                5 to "june5",
                6 to "june6"
            )
		)
	).map { (year, months) ->
        val c = Calendar.getInstance()
        c[Calendar.YEAR] = year
        months.map { (month, days) ->
            c[Calendar.MONTH] = month
            days.map { (dayOfMonth, payload) ->
                c[Calendar.DAY_OF_MONTH] = dayOfMonth
                c[Calendar.HOUR_OF_DAY] = 12
                c[Calendar.MINUTE] = 15
                val t = c.timeInMillis
                Event(startTime = t, endTime = t + 1_000 * 60 * 75, type = payload)
            }
        }.flatten()
    }.flatten()

    private fun showToast(message: String) {
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun px(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun monthScrollerView(context: Context): View {
        val result = MonthScrollerView(context)

        result.setFirstDayOfWeek(value = Calendar.MONDAY)
        result.toSkipEmptyMonths(value = false)
        result.toSkipEmptyTodayMonth(value = false)
        result.toSelectTodayAuto(value = true)

        result.setDayHeight(value = px(dp = 29f))
        result.setDayPaddingTop(value = px(dp = 6f))
        result.setDayPaddingBottom(value = px(dp = 10f))
        result.setDayTextSize(value = px(dp = 15f))
        result.setDayTypefaceRegular(value = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)!!)
        result.setDayTypefaceWeekend(value = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)!!)
        result.setDayTextColorRegular(value = Color.parseColor("#000000"))
        result.setDayTextColorSelected(value = Color.parseColor("#ffffff"))
        result.setDayTextColorToday(value = Color.parseColor("#e91e42"))
        result.setDaySelectedColorRegular(value = Color.parseColor("#7092ac"))
        result.setDaySelectedColorToday(value = Color.parseColor("#af1833"))

        result.toDrawPayload(value = true)
        result.setPayloadHeight(value = px(dp = 20f))
        result.setPayloadMargin(value = px(dp = 5f))
        result.setPayloadTextSize(value = px(dp = 12f))
        result.setPayloadTextColor(value = Color.parseColor("#36709c"))
        result.setPayloadBackgroundColor(value = Color.parseColor("#e0ebf4"))
        result.toSelectPayloadEmpty(value = true)
        val calendar = Calendar.getInstance()
        val payload: Map<Int, Map<Int, Map<Int, String>>> = events.map {
            calendar.timeInMillis = it.startTime
            Triple(calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]) to it.type
        }.groupBy { (triple, _) ->
            val (year, _, _) = triple
            year
        }.mapValues { (_, months) ->
            months.groupBy { (triple, _) ->
                val (_, month, _) = triple
                month
            }.mapValues { (_, days) ->
                days.map { (triple, type) ->
                    val (_, _, dayOfMonth) = triple
                    dayOfMonth to type
                }.toMap()
            }
        }
        result.setPayload(value = payload)

        result.setLineTypeHorizontal(value = MonthScrollerView.LineTypeHorizontal.REGULAR)
        result.setLineColor(value = Color.parseColor("#cccccc"))
        result.setLineSize(value = px(dp = 1f))
        result.setNonActiveAlpha(value = (255 * 0.5f).toInt())

        result.onMonthChange = { year, month ->
            val value = String.format("%04d/%02d", year, month)
            showToast(value)
        }
        result.toChangeSelectedDate(value = false)
        result.onSelectDate = { year, month, dayOfMonth ->
            val value = String.format("%04d/%02d/%02d", year, month, dayOfMonth)
            showToast(value)
        }

        return result
    }
    private fun weekScrollerView(context: Context): View {
        val result = WeekScrollerView(context)

        result.setPadding(0, px(dp = 9f).toInt(), 0, px(dp = 1f).toInt())
        result.setFirstDayOfWeek(value = Calendar.MONDAY)
        result.toSkipEmptyWeeks(value = false)
        result.toSkipEmptyTodayWeek(value = false)
        result.toSelectTodayAuto(value = true)

        result.toDrawDayName(value = true)
        result.setDayHeight(value = px(dp = 29f))
        result.setDaySelectedColorRegular(value = Color.parseColor("#7092ac"))
        result.setDaySelectedColorToday(value = Color.parseColor("#af1833"))
        result.setDayTextSize(value = px(dp = 15f))
        result.setDayTextColorRegular(value = Color.parseColor("#000000"))
        result.setDayTextColorSelected(value = Color.parseColor("#ffffff"))
        result.setDayTextColorToday(value = Color.parseColor("#e91e42"))
        result.setDayTypefaceRegular(value = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)!!)
        result.setDayTypefaceWeekend(value = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)!!)
        result.setDayNameHeight(value = px(dp = 10f))
        result.setDayNameMargin(value = px(dp = 5f))
        result.setDayNameTextSize(value = px(dp = 10f))
        result.setDayNameTextColor(value = Color.parseColor("#000000"))
        result.setDayOfWeekToString { dayOfWeek ->
            when (dayOfWeek) {
                Calendar.MONDAY -> "ПН"
                Calendar.TUESDAY -> "ВТ"
                Calendar.WEDNESDAY -> "СР"
                Calendar.THURSDAY -> "ЧТ"
                Calendar.FRIDAY -> "ПТ"
                Calendar.SATURDAY -> "СБ"
                Calendar.SUNDAY -> "ВС"
                else -> error("Day of week \"$dayOfWeek\" not supported!")
            }
        }

        result.toDrawPayload(value = true)
        result.setPayloadHeight(px(dp = 3f))
        result.setPayloadMargin(px(dp = 2f))
        result.setPayloadColor(value = Color.parseColor("#af1833"))
        val calendar = Calendar.getInstance()
        val payload: Map<Int, Map<Int, Map<Int, String>>> = events.map {
            calendar.timeInMillis = it.startTime
            Triple(calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]) to it.type
        }.groupBy { (triple, _) ->
            val (year, _, _) = triple
            year
        }.mapValues { (_, months) ->
            months.groupBy { (triple, _) ->
                val (_, month, _) = triple
                month
            }.mapValues { (_, days) ->
                days.map { (triple, type) ->
                    val (_, _, dayOfMonth) = triple
                    dayOfMonth to type
                }.toMap()
            }
        }
        result.setPayload(value = payload)

        result.onWeekChange = { year, weekOfYear ->
            val value = String.format("%04d/%02d", year, weekOfYear)
            showToast(value)
        }
        result.toChangeSelectedDate(value = true)
        result.onSelectDate = { year, month, dayOfMonth ->
            val value = String.format("%04d/%02d/%02d", year, month, dayOfMonth)
            showToast(value)
        }

        return result
    }
    private fun scheduleView(context: Context): View {
        val result = ScheduleView(context)

        val timeRange = (8 * 60)..(8 * 60 + 10 * 60)
        val target = Calendar.getInstance().also {
            it.timeInMillis = events.minBy { event -> event.startTime }!!.startTime
        }

        result.setPadding(0, px(dp = 14f).toInt(), 0, px(dp = 14f).toInt())
        result.setTimeRange(start = timeRange.start, endInclusive = timeRange.endInclusive)
        result.setTimeTextMargin(px(dp = 16f))
        result.setTimeTextSize(value = px(dp = 11f))
        result.setTimeTextColor(value = Color.parseColor("#969696"))

        result.setTimeStepMinutes(value = 30)
        result.setTimeStepHeight(value = px(dp = 40f))

        result.setTimeMarkColor(value = Color.parseColor("#af1833"))
        result.setTimeMarkSize(value = px(dp = 1f))
        result.setTimeMarkAlpha(value = 255 / 100 * 50)
        result.setTimeMarkRadius(value = px(dp = 5f) / 2)
        result.setTimeMarkAuto(year = target[Calendar.YEAR], month = target[Calendar.MONTH], dayOfMonth = target[Calendar.DAY_OF_MONTH])

        result.setTimeLineMarginStart(value = px(dp = 52f))
        result.setTimeLineMarginEnd(value = px(dp = 8f))
        result.setTimeLineCount(value = 1)
        result.setTimeLineColor(value = Color.parseColor("#cccccc"))
        result.setTimeLineSize(value = px(dp = 0.5f))

        result.setGroupMargin(value = px(dp = 3f))
        result.setGroupMarginStart(value = px(dp = 54f))
        result.setGroupMarginEnd(value = px(dp = 16f))
        result.setGroupPaddingTop(value = px(dp = 3.5f))
        result.setGroupPaddingStart(value = px(dp = 6f))
        result.setGroupPaddingEnd(value = px(dp = 4f))
        result.setGroupBackgroundColor(value = Color.parseColor("#accde6"))
        result.setGroupBackgroundAlpha(isActive = true, value = 255 / 100 * 35)
        result.setGroupBackgroundAlpha(isActive = false, value = 255 / 100 * 25)
        result.setGroupForegroundColor(value = Color.parseColor("#36709c"))
        result.setGroupForegroundAlpha(isActive = true, value = 255 / 100 * 100)
        result.setGroupForegroundAlpha(isActive = false, value = 255 / 100 * 60)
        result.setGroupRadius(value = px(dp = 2f))
        result.setGroupLineCountMax(value = 3)
        result.setGroupTextSize(value = px(dp = 11f))
        result.setGroupTextLineSpace(value = px(dp = 6f))

        val calendar = Calendar.getInstance()
        val payload: List<Triple<Int, Int, String>> = events.filter {
            calendar.timeInMillis = it.startTime
            val sy = calendar[Calendar.YEAR]
            val sm = calendar[Calendar.MONTH]
            val sd = calendar[Calendar.DAY_OF_MONTH]
            val sMinutes = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]
            calendar.timeInMillis = it.endTime
            val ey = calendar[Calendar.YEAR]
            val em = calendar[Calendar.MONTH]
            val ed = calendar[Calendar.DAY_OF_MONTH]
            val eMinutes = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]
            sy == target[Calendar.YEAR] && ey == target[Calendar.YEAR] &&
            sm == target[Calendar.MONTH] && em == target[Calendar.MONTH] &&
            sd == target[Calendar.DAY_OF_MONTH] && ed == target[Calendar.DAY_OF_MONTH] &&
            sMinutes >= timeRange.start &&
            eMinutes <= timeRange.endInclusive
        }.map {
            calendar.timeInMillis = it.startTime
            val sh = calendar[Calendar.HOUR_OF_DAY]
            val sm = calendar[Calendar.MINUTE]
            calendar.timeInMillis = it.endTime
            val eh = calendar[Calendar.HOUR_OF_DAY]
            val em = calendar[Calendar.MINUTE]
            Triple(sh * 60 + sm, eh * 60 + em, it.type)
        }
        result.setPayload(list = payload)

        result.onGroupClick = { start, end ->
            val event = events.filter {
                calendar.timeInMillis = it.startTime
                val sy = calendar[Calendar.YEAR]
                val sm = calendar[Calendar.MONTH]
                val sd = calendar[Calendar.DAY_OF_MONTH]
                val sMinutes = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]
                calendar.timeInMillis = it.endTime
                val ey = calendar[Calendar.YEAR]
                val em = calendar[Calendar.MONTH]
                val ed = calendar[Calendar.DAY_OF_MONTH]
                val eMinutes = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]
                sy == target[Calendar.YEAR] && ey == target[Calendar.YEAR] &&
                sm == target[Calendar.MONTH] && em == target[Calendar.MONTH] &&
                sd == target[Calendar.DAY_OF_MONTH] && ed == target[Calendar.DAY_OF_MONTH] &&
                start <= sMinutes &&
                end >= eMinutes
            }.minBy { it.startTime }
            if (event == null) {
                Toast.makeText(context, "null", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, event.type, Toast.LENGTH_SHORT).show()
            }
        }

        return result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context: Context = this
//        val view = monthScrollerView(context)
//        val view = weekScrollerView(context)
        val view = scheduleView(context)
        setContentView(FrameLayout(context).also {
            it.background = ColorDrawable(Color.BLACK)
            view.background = ColorDrawable(Color.WHITE)
            it.addView(view)
        })
    }
}
