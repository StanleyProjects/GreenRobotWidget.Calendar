package sp.service.sample

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import java.util.Calendar
import java.util.TimeZone
import org.json.JSONArray
import org.json.JSONObject
import sp.grw.calendar.MonthScrollerView
import sp.grw.calendar.ScheduleView
import sp.grw.calendar.WeekScrollerView
import sp.grw.calendar.entity.ActiveType

class MainActivity : Activity() {
    private class Event(val startTime: Long, val endTime: Long, val type: String)

    companion object {
        private val timeZoneSource = TimeZone.getTimeZone("UTC")
//        private val timeZoneTarget = TimeZone.getDefault()
        private val timeZoneTarget = timeZoneSource
        private const val firstDayOfWeek = Calendar.MONDAY
//        private const val firstDayOfWeek = Calendar.TUESDAY
    }
/*
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
        val c = Calendar.getInstance(timeZoneSource)
        c[Calendar.YEAR] = year
        months.map { (month, days) ->
            c[Calendar.MONTH] = month
            days.map { (dayOfMonth, payload) ->
                c[Calendar.DAY_OF_MONTH] = dayOfMonth
                c[Calendar.HOUR_OF_DAY] = 12
                c[Calendar.MINUTE] = 15
                val t = c.timeInMillis
                (0 until 3).map {
                    Event(startTime = t + it * 1_000 * 60 * 30, endTime = t + 1_000 * 60 * 75 + it * 1_000 * 60 * 30, type = payload)
                }
            }.flatten()
        }.flatten()
    }.flatten()
*/

    private val events = JSONArray("""
        [
          {
            "f": 1620460800,
            "t": 1620464400,
            "title": "Встреча"
          },
          {
            "f": 1619521200,
            "t": 1619524080,
            "title": "Встреча"
          },
          {
            "f": 1619168400,
            "t": 1619172000,
            "title": "Встреча"
          },
          {
            "f": 1619168400,
            "t": 1619172000,
            "title": "Встреча"
          },
          {
            "f": 1619082000,
            "t": 1619085600,
            "title": "Встреча"
          },
          {
            "f": 1619082000,
            "t": 1619085600,
            "title": "Встреча"
          },
          {
            "f": 1619082000,
            "t": 1619085600,
            "title": "Встреча"
          },
          {
            "f": 1618668000,
            "t": 1618669800,
            "title": "Осмотр"
          }
        ]
    """.trimIndent()).let { array ->
        val time = Calendar.getInstance().also {
            it.timeZone = timeZoneSource
            it[Calendar.HOUR_OF_DAY] = 9
            it[Calendar.MINUTE] = 0
            it[Calendar.SECOND] = 0
        }.timeInMillis
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val minutes = 5
//            val minutes = 10
//            val minutes = 15
//            val minutes = 20
            (0..0).map { times ->
                Event(
                    startTime = time + index * 1_000 * 60 * 30 + times * 1_000 * 60 * 10,
                    endTime   = time + index * 1_000 * 60 * 30 + times * 1_000 * 60 * 10 + 1_000 * 60 * minutes,
                    type = item.getString("title") + " #$index/$times"
                )
            }
        }.flatten()
    }

    private fun showToast(message: String) {
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun px(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun monthScrollerView(context: Context): View {
        val result = MonthScrollerView(context)

        val timeZone = TimeZone.getDefault()

        result.setTimeZone(value = timeZoneTarget)
        result.setFirstDayOfWeek(value = firstDayOfWeek)
        result.toSkipEmptyMonths(value = false)
        result.toSkipEmptyTodayMonth(value = false)
        result.toSelectTodayAuto(value = true)
        result.setActiveType(value = ActiveType.FUTURE)
//        result.setActiveType(value = ActiveType.PAYLOAD)
        result.setMonthOffsetBefore(value = 1)

        result.setDayHeight(value = px(dp = 29f))
        result.setDayPaddingTop(value = px(dp = 6f))
        result.setDayPaddingBottom(value = px(dp = 10f))
        result.setDayTextSize(value = px(dp = 15f))
        result.setDayTypefaceRegular(value = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)!!)
        result.setDayTypefaceWeekend(value = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)!!)
        result.setDayTextColorRegular(value = Color.parseColor("#000000"))
        result.setDayTextColorNotActive(value = Color.parseColor("#b2b2b2"))
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
        val calendar = Calendar.getInstance(timeZone)
        val items = JSONArray(items).let {
            (0 until it.length()).map { index -> it.getJSONObject(index) }
        }
        val payload: Map<Int, Map<Int, Map<Int, String>>> = items.map {
            calendar.timeInMillis = it.getLong("dateFrom") * 1_000
            Triple(calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]) to it
        }.groupBy { (triple, _) ->
            val (year, _, _) = triple
            year
        }.mapValues { (_, months) ->
            months.groupBy { (triple, _) ->
                val (_, month, _) = triple
                month
            }.mapValues { (_, days) ->
                days.groupBy { (triple, _) ->
                    val (_, _, dayOfMonth) = triple
                    dayOfMonth
                }.mapValues { (_, list) ->
                    list.size.toString()
                }
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
        result.toChangeSelectedDate(value = true)
        result.onSelectDate = { year, month, dayOfMonth ->
//            val value = String.format("%04d/%02d/%02d", year, month, dayOfMonth)
            val value = items.filter {
                calendar.timeZone = timeZoneSource
                calendar.timeInMillis = it.getLong("dateFrom") * 1_000
                calendar.timeZone = timeZoneTarget
                calendar[Calendar.YEAR] == year && calendar[Calendar.MONTH] == month && calendar[Calendar.DAY_OF_MONTH] == dayOfMonth
            }.joinToString {
                calendar.timeZone = timeZoneSource
                calendar.timeInMillis = it.getLong("dateFrom") * 1_000
                calendar.timeZone = timeZoneTarget
                String.format("%02d:%02d", calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE])
            }
            showToast(value)
        }

        return LinearLayout(context).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.orientation = LinearLayout.VERTICAL
            it.addView(result)
            it.addView(Button(context).also { button ->
                button.text = "select today"
                button.setOnClickListener {
                    val c = Calendar.getInstance().also { c ->
                        c.firstDayOfWeek = firstDayOfWeek
                        c.timeZone = timeZoneTarget
                    }
                    result.selectDate(year = c[Calendar.YEAR], month = c[Calendar.MONTH], dayOfMonth = c[Calendar.DAY_OF_MONTH], toMove = false)
                }
            })
            it.addView(Button(context).also { button ->
                button.text = "select today month"
                button.setOnClickListener {
                    val c = Calendar.getInstance().also { c ->
                        c.firstDayOfWeek = firstDayOfWeek
                        c.timeZone = timeZoneTarget
                    }
                    result.setYearMonth(year = c[Calendar.YEAR], month = c[Calendar.MONTH])
                }
            })
            it.addView(Button(context).also { button ->
                button.text = "to month selected"
                button.setOnClickListener {
                    result.setYearMonthSelected()
                }
            })
        }
    }
    private fun weekScrollerView(context: Context): View {
        val result = WeekScrollerView(context)

        result.setPadding(0, px(dp = 9f).toInt(), 0, px(dp = 1f).toInt())
        result.setTimeZone(value = timeZoneTarget)
        result.setFirstDayOfWeek(value = firstDayOfWeek)
        result.toSkipEmptyWeeks(value = false)
        result.toSkipEmptyTodayWeek(value = false)
        result.toSelectTodayAuto(value = true)
        result.setActiveType(value = ActiveType.ALL)
        result.setMonthOffsetBefore(value = 2)
        result.setMonthOffsetAfter(value = 1)

        result.toDrawDayName(value = true)
        result.setDayHeight(value = px(dp = 29f))
        result.setDaySelectedColorRegular(value = Color.parseColor("#7092ac"))
        result.setDaySelectedColorToday(value = Color.parseColor("#af1833"))
        result.setDayTextSize(value = px(dp = 15f))
        result.setDayTextColorRegular(value = Color.parseColor("#000000"))
        result.setDayTextColorNotActive(value = Color.parseColor("#b2b2b2"))
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
        result.setPayloadHeight(value = px(dp = 3f))
        result.setPayloadMargin(value = px(dp = 2f))
        result.setPayloadColor(value = Color.parseColor("#af1833"))
        result.toSelectPayloadEmpty(value = true)
        val calendar = Calendar.getInstance()
        val json = "{\"data\":{\"items\":[]}}"
        val events = JSONObject(json).getJSONObject("data").getJSONArray("items").let {
            (0 until it.length()).map { index -> it.getJSONObject(index) }
        }
        val payload: Map<Int, Map<Int, Map<Int, String>>> = events.map {
            calendar.timeInMillis = (it.getLong("dateFrom") ?: 0) * 1000
            Triple(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            ) to (it.getJSONObject("type").getString("title") ?: "")
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
        result.selectDate(
            year = calendar[Calendar.YEAR],
            month = calendar[Calendar.MONTH],
            dayOfMonth = calendar[Calendar.DAY_OF_MONTH],
            toMove = true
        )

        result.onWeekChange = { year, weekOfYear ->
            val value = String.format("%04d/%02d", year, weekOfYear)
            showToast(value)
        }
        result.toChangeSelectedDate(value = true)
        result.onSelectDate = { year, month, dayOfMonth ->
//            val value = String.format("%04d/%02d/%02d", year, month, dayOfMonth)
            val value = events.filter {
                calendar.timeZone = timeZoneSource
                calendar.timeInMillis = it.getLong("dateFrom")
                calendar.timeZone = timeZoneTarget
                calendar[Calendar.YEAR] == year && calendar[Calendar.MONTH] == month && calendar[Calendar.DAY_OF_MONTH] == dayOfMonth
            }.joinToString {
                calendar.timeZone = timeZoneSource
                calendar.timeInMillis = it.getLong("dateFrom")
                calendar.timeZone = timeZoneTarget
                String.format("%02d:%02d", calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE])
            }
            showToast(value)
        }

        return LinearLayout(context).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.orientation = LinearLayout.VERTICAL
            it.addView(result)
            it.addView(Button(context).also { button ->
                button.text = "select today"
                button.setOnClickListener {
                    val c = Calendar.getInstance().also { c ->
                        c.firstDayOfWeek = firstDayOfWeek
                        c.timeZone = timeZoneTarget
                    }
                    result.selectDate(year = c[Calendar.YEAR], month = c[Calendar.MONTH], dayOfMonth = c[Calendar.DAY_OF_MONTH], toMove = false)
                }
            })
            it.addView(Button(context).also { button ->
                button.text = "select today week"
                button.setOnClickListener {
                    val c = Calendar.getInstance().also { c ->
                        c.firstDayOfWeek = firstDayOfWeek
                        c.timeZone = timeZoneTarget
                    }
                    result.setYearWeek(year = c[Calendar.YEAR], weekOfYear = c[Calendar.WEEK_OF_YEAR])
                }
            })
            it.addView(Button(context).also { button ->
                button.text = "to week selected"
                button.setOnClickListener {
                    result.setYearWeekSelected()
                }
            })
        }
    }
    private fun scheduleView(context: Context): View {
        val result = ScheduleView(context)

        val timeRange = (8 * 60)..(8 * 60 + 10 * 60)
        val target = Calendar.getInstance(timeZoneTarget).also {
            it.timeInMillis = events.minOfOrNull { event -> event.startTime }!!
        }

        result.setTimeZone(value = timeZoneTarget)
        result.setPadding(0, px(dp = 14f).toInt(), 0, px(dp = 14f).toInt())
        result.setTimeRange(start = timeRange.start, endInclusive = timeRange.endInclusive)
        result.setTimeTextMargin(value = px(dp = 16f))
        result.setTimeTextSize(value = px(dp = 11f))
        result.setTimeTextColor(value = Color.parseColor("#969696"))

        result.setTimeStepMinutes(value = 15)
//        result.setTimeStepMinutes(value = 30)
        result.setTimeStepHeight(value = px(dp = 40f))

        result.setTimeMarkColor(value = Color.parseColor("#af1833"))
        result.setTimeMarkSize(value = px(dp = 1f))
        result.setTimeMarkAlpha(value = 255 / 100 * 50)
        result.setTimeMarkRadius(value = px(dp = 5f) / 2)
        result.setTimeMarkAuto(year = target[Calendar.YEAR], month = target[Calendar.MONTH], dayOfMonth = target[Calendar.DAY_OF_MONTH])

        result.setTimeLineMarginStart(value = px(dp = 52f))
        result.setTimeLineMarginEnd(value = px(dp = 8f))
        result.setTimeLineCount(value = 0)
//        result.setTimeLineCount(value = 1)
        result.setTimeLineColor(value = Color.parseColor("#cccccc"))
        result.setTimeLineSize(value = px(dp = 0.5f))

        result.setGroupMargin(value = px(dp = 3f))
        result.setGroupMarginStart(value = px(dp = 54f))
        result.setGroupMarginEnd(value = px(dp = 16f))
        result.setGroupPaddingTop(value = px(dp = 3.5f))
        result.setGroupPaddingStart(value = px(dp = 6f))
        result.setGroupPaddingEnd(value = px(dp = 4f))
        result.setGroupBackgroundColor(value = Color.parseColor("#accde6"))
        result.setGroupBackgroundAlpha(isActive = true, value = (255f / 100 * 35).toInt())
        result.setGroupBackgroundAlpha(isActive = false, value = (255f / 100 * 25).toInt())
        result.setGroupForegroundColor(value = Color.parseColor("#36709c"))
        result.setGroupForegroundAlpha(isActive = true, value = (255f / 100 * 100).toInt())
        result.setGroupForegroundAlpha(isActive = false, value = (255f / 100 * 60).toInt())
        result.setGroupRadius(value = px(dp = 2f))
//        result.setGroupLineCountMax(value = 3)
        result.setGroupLineCountMaxByGroupHeight(value = true)
        result.setGroupTextSize(value = px(dp = 11f))
        result.setGroupTextLineSpace(value = px(dp = 6f))
        val timeMinimumInMinutes = 15
        result.setGroupMinHeightInMinutes(value = timeMinimumInMinutes)

        val calendar = Calendar.getInstance()
        val payload: List<Triple<Int, Int, String>> = events.filter {
            calendar.timeZone = timeZoneSource
            calendar.timeInMillis = it.startTime
            calendar.timeZone = timeZoneTarget
            val sy = calendar[Calendar.YEAR]
            val sm = calendar[Calendar.MONTH]
            val sd = calendar[Calendar.DAY_OF_MONTH]
            val sMinutes = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]
            calendar.timeZone = timeZoneSource
            calendar.timeInMillis = it.endTime
            calendar.timeZone = timeZoneTarget
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
            calendar.timeZone = timeZoneSource
            calendar.timeInMillis = it.startTime
            calendar.timeZone = timeZoneTarget
            val sMinutes = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]
            calendar.timeZone = timeZoneSource
            calendar.timeInMillis = it.endTime
            calendar.timeZone = timeZoneTarget
            val eMinutes = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]
            Triple(sMinutes, eMinutes, it.type)
        }
        result.setPayload(list = payload, timeMinimumInMinutes = timeMinimumInMinutes)

        result.onGroupClick = { start, end ->
            calendar.timeZone = timeZoneTarget
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
            }.minByOrNull { it.startTime }
            if (event == null) {
                showToast("null")
            } else {
                showToast(event.type)
            }
        }

        return result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context: Context = this
        val view = monthScrollerView(context)
//        val view = weekScrollerView(context)
//        val view = scheduleView(context)
        setContentView(FrameLayout(context).also {
            it.background = ColorDrawable(Color.BLACK)
            view.background = ColorDrawable(Color.WHITE)
            it.addView(view)
        })
    }
}
