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

        result.toDrawnPayload(value = true)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context: Context = this
        val view = monthScrollerView(context)
        setContentView(FrameLayout(context).also {
            it.background = ColorDrawable(Color.BLACK)
            view.background = ColorDrawable(Color.WHITE)
            it.addView(view)
        })
    }
}
