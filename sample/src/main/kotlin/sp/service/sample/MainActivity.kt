package sp.service.sample

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import java.util.Calendar
import sp.grw.calendar.MonthScrollerView

class MainActivity : Activity() {
    private fun px(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun monthScrollerView(context: Context): View {
        val result = MonthScrollerView(context)

        result.setDayHeight(value = px(dp = 29f))
        result.setDayPaddingTop(value = px(dp = 6f))
        result.setDayPaddingBottom(value = px(dp = 10f))
        result.setDayTextSize(value = px(dp = 15f))
        result.setDayTypefaceWeekend(value = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)!!)
        result.setDaySelectedColorRegular(Color.parseColor("#7092ac"))
        result.setDaySelectedColorToday(Color.parseColor("#af1833"))

        result.toDrawnPayload(value = true)
        result.setPayloadHeight(value = px(dp = 20f))
        result.setPayloadMargin(value = px(dp = 5f))
        result.setPayloadTextSize(value = px(dp = 12f))
        result.setPayloadTextColor(value = Color.parseColor("#36709c"))
        result.setPayloadBackgroundColor(value = Color.parseColor("#e0ebf4"))
        val payload = mapOf(
        	2021 to mapOf(
        		Calendar.MAY to mapOf(
                    2 to "may2",
                    3 to "may3",
                    4 to "may4"
                )
    		)
    	)
        result.setPayload(payload)

        result.setLineTypeHorizontal(MonthScrollerView.LineTypeHorizontal.REGULAR)
        result.setLineColor(value = Color.parseColor("#cccccc"))
        result.setLineSize(value = px(dp = 1f))
        result.setNonActiveAlpha(value = (255 * 0.5f).toInt())

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
