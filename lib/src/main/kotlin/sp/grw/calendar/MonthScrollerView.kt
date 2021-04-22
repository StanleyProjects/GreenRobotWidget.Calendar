package sp.grw.calendar

import android.content.Context
import android.view.View

class MonthScrollerView(context: Context) : View(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(0, 0) // todo
    }
}
