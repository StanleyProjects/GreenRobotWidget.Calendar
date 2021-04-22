package sp.service.sample

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import sp.grw.calendar.MonthScrollerView

class MainActivity : Activity() {
    private fun monthScrollerView(context: Context): View {
        val result = MonthScrollerView(context)
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
