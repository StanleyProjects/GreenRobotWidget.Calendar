package sp.grw.calendar

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import java.util.Calendar
import kotlin.math.absoluteValue
import sp.grw.calendar.entity.Payload
import sp.grw.calendar.entity.YearMonthDay
import sp.grw.calendar.entity.YearWeek
import sp.grw.calendar.util.AndroidUtil.getTextHeight
import sp.grw.calendar.util.DateUtil
import sp.grw.calendar.util.DateUtil.toYearMonthDay
import sp.grw.calendar.util.DateUtil.toYearWeek

class WeekScrollerView(context: Context) : View(context) {
    companion object {
        private val DEFAULT_DAY_OF_WEEK_TO_STRING: (Int) -> String = { dayOfWeek ->
            when (dayOfWeek) {
                Calendar.MONDAY -> "MO"
                Calendar.TUESDAY -> "TU"
                Calendar.WEDNESDAY -> "WE"
                Calendar.THURSDAY -> "TH"
                Calendar.FRIDAY -> "FR"
                Calendar.SATURDAY -> "SA"
                Calendar.SUNDAY -> "SU"
                else -> error("Day of week \"$dayOfWeek\" not supported!")
            }
        }

        /**
         * @return Map<Int(Year), Set<Int(Week)>>
         */
        private fun allWeeks(
            firstDayOfWeek: Int,
            minYear: Int,
            minWeek: Int,
            maxYear: Int,
            maxWeek: Int
        ): Map<Int, Set<Int>> {
            if (minYear == maxYear) return mapOf(minYear to (minWeek..maxWeek).toSet())
            val result = mutableMapOf<Int, Set<Int>>()
            val calendar = Calendar.getInstance()
            calendar.firstDayOfWeek = firstDayOfWeek
            calendar[Calendar.YEAR] = minYear
            result[minYear] = (minWeek..calendar.getActualMaximum(Calendar.WEEK_OF_YEAR)).toSet()
            for(year in (minYear + 1) until maxYear) {
                calendar[Calendar.YEAR] = year
                result[year] = (calendar.getActualMinimum(Calendar.WEEK_OF_YEAR)..calendar.getActualMaximum(Calendar.WEEK_OF_YEAR)).toSet()
            }
            calendar[Calendar.YEAR] = maxYear
            result[maxYear] = (calendar.getActualMinimum(Calendar.WEEK_OF_YEAR)..maxWeek).toSet()
            return result
        }
        /**
         * @return Map<Int(Year), Set<Int(Week)>>
         */
        private fun allWeeks(
            payload: Payload,
            firstDayOfWeek: Int,
            isEmptyTodayWeekSkipped: Boolean
        ): Map<Int, Set<Int>> {
            if (payload.isEmpty()) {
                return if (isEmptyTodayWeekSkipped) emptyMap()
                else {
                    val calendar = Calendar.getInstance()
                    mapOf(calendar[Calendar.YEAR] to setOf(calendar[Calendar.MONTH]))
                }
            }
            val minYear = payload.getYears().min()!!
            val minWeek = payload.getWeeks(year = minYear, firstDayOfWeek = firstDayOfWeek).min()!!
            val maxYear = payload.getYears().max()!!
            val maxWeek = payload.getWeeks(year = maxYear, firstDayOfWeek = firstDayOfWeek).max()!!
            if (isEmptyTodayWeekSkipped) {
                return allWeeks(
                    firstDayOfWeek = firstDayOfWeek,
                    minYear = minYear,
                    minWeek = minWeek,
                    maxYear = maxYear,
                    maxWeek = maxWeek
                )
            }
            val today = Calendar.getInstance().toYearWeek(firstDayOfWeek = firstDayOfWeek)
            val min = when {
                minYear > today.year -> today
                minYear == today.year -> YearWeek(year = minYear, weekOfYear = kotlin.math.min(minWeek, today.weekOfYear))
                else -> YearWeek(year = minYear, weekOfYear = minWeek)
            }
            val max = when {
                maxYear < today.year -> today
                maxYear == today.year -> YearWeek(year = maxYear, weekOfYear = kotlin.math.max(maxWeek, today.weekOfYear))
                else -> YearWeek(year = maxYear, weekOfYear = maxWeek)
            }
            return allWeeks(
                firstDayOfWeek = firstDayOfWeek,
                minYear = min.year,
                minWeek = min.weekOfYear,
                maxYear = max.year,
                maxWeek = max.weekOfYear
            )
        }
        /**
         * @return Map<Int(Year), Set<Int(Week)>>
         */
        private fun onlyWeeksWithDays(
            payload: Payload,
            firstDayOfWeek: Int,
            isEmptyTodayWeekSkipped: Boolean
        ): Map<Int, Set<Int>> {
            val result = mutableMapOf<Int, Set<Int>>()
            payload.forEachWeeks(firstDayOfWeek) { year, weeks ->
                result[year] = weeks
            }
            if (!isEmptyTodayWeekSkipped) {
                val today = Calendar.getInstance().toYearWeek(firstDayOfWeek = firstDayOfWeek)
                val weeks = result[today.year].orEmpty() + today.weekOfYear
                result[today.year] = weeks.sortedBy { it }.toSet()
            }
            return result
        }
        /**
         * @return Map<Int(Year), Set<Int(Week)>>
         */
        private fun getWeeks(
            payload: Payload,
            firstDayOfWeek: Int,
            isEmptyWeeksSkipped: Boolean,
            isEmptyTodayWeekSkipped: Boolean
        ): Map<Int, Set<Int>> {
            return if (isEmptyWeeksSkipped) {
                onlyWeeksWithDays(
                    payload = payload,
                    firstDayOfWeek = firstDayOfWeek,
                    isEmptyTodayWeekSkipped = isEmptyTodayWeekSkipped
                )
            } else {
                allWeeks(
                    payload = payload,
                    firstDayOfWeek = firstDayOfWeek,
                    isEmptyTodayWeekSkipped = isEmptyTodayWeekSkipped
                )
            }
        }

        private fun getYearWeekDefault(
            payload: Payload,
            firstDayOfWeek: Int,
            isEmptyWeeksSkipped: Boolean,
            isEmptyTodayWeekSkipped: Boolean
        ): YearWeek? {
            val today = Calendar.getInstance().toYearWeek(firstDayOfWeek = firstDayOfWeek)
            if (!isEmptyTodayWeekSkipped) return today
            val weeks = getWeeks(
                payload = payload,
                firstDayOfWeek = firstDayOfWeek,
                isEmptyWeeksSkipped = isEmptyWeeksSkipped,
                isEmptyTodayWeekSkipped = isEmptyTodayWeekSkipped
            )
            val ws = weeks[today.year]
            if (ws != null && ws.any { it == today.weekOfYear }) return today
            val year = weeks.keys.firstOrNull() ?: return null
            val week = weeks[year]?.firstOrNull() ?: return null
            return YearWeek(year = year, weekOfYear = week)
        }
    }

    private var dayHeight = 0f
    fun setDayHeight(value: Float) {
        if (value < 0) error("Negative height!")
        dayHeight = value
        invalidate()
    }
    private val dayPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun setDayTextSize(value: Float) {
        dayPaint.textSize = value
        invalidate()
    }
    private val daySelectedPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var daySelectedColorRegular = Color.BLACK
    fun setDaySelectedColorRegular(value: Int) {
        daySelectedColorRegular = value
        invalidate()
    }
    private var daySelectedColorToday = Color.RED
    fun setDaySelectedColorToday(value: Int) {
        daySelectedColorToday = value
        invalidate()
    }
    private var dayTextColorRegular = Color.BLACK
    fun setDayTextColorRegular(value: Int) {
        dayTextColorRegular = value
        invalidate()
    }
    private var dayTextColorSelected = Color.WHITE
    fun setDayTextColorSelected(value: Int) {
        dayTextColorSelected = value
        invalidate()
    }
    private var dayTextColorToday = Color.RED
    fun setDayTextColorToday(value: Int) {
        dayTextColorToday = value
        invalidate()
    }
    private var dayTypefaceRegular = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)!!
    fun setDayTypefaceRegular(value: Typeface) {
        dayTypefaceRegular = value
        invalidate()
    }
    private var dayTypefaceWeekend = dayTypefaceRegular
    fun setDayTypefaceWeekend(value: Typeface) {
        dayTypefaceWeekend = value
        invalidate()
    }

    private var isDayNameDrawn: Boolean = false
    fun toDrawDayName(value: Boolean) {
        isDayNameDrawn = value
        invalidate()
    }
    private var dayNameHeight = 0f
    fun setDayNameHeight(value: Float) {
        if (value < 0) error("Negative height!")
        dayNameHeight = value
        invalidate()
    }
    private var dayNameMargin = 0f
    fun setDayNameMargin(value: Float) {
        if (value < 0) error("Negative margin!")
        dayNameMargin = value
        invalidate()
    }
    private val dayNamePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun setDayNameTextSize(value: Float) {
        dayNamePaint.textSize = value
        dayNameHeight = kotlin.math.max(value, dayNameHeight)
        invalidate()
    }
    fun setDayNameTextColor(value: Int) {
        dayNamePaint.color = value
        invalidate()
    }
    private var dayOfWeekToString: (Int) -> String = DEFAULT_DAY_OF_WEEK_TO_STRING
    fun setDayOfWeekToString(map: (Int) -> String) {
        dayOfWeekToString = map
        invalidate()
    }

    private var payload: Payload = Payload(emptyMap())
    fun setPayload(value: Map<Int, Map<Int, Map<Int, String>>>) {
        payload = Payload(value)
        yearWeekCurrent = getYearWeekDefault(
            payload = payload,
            firstDayOfWeek = firstDayOfWeek,
            isEmptyWeeksSkipped = isEmptyWeeksSkipped,
            isEmptyTodayWeekSkipped = isEmptyTodayWeekSkipped
        )
        invalidate()
    }
    private var isPayloadDrawn: Boolean = false
    fun toDrawPayload(value: Boolean) {
        isPayloadDrawn = value
        invalidate()
    }
    private var payloadHeight = 0f
    fun setPayloadHeight(value: Float) {
        if (value < 0) error("Negative height!")
        payloadHeight = value
        invalidate()
    }
    private var payloadMargin = 0f
    fun setPayloadMargin(value: Float) {
        if (value < 0) error("Negative margin!")
        payloadMargin = value
        invalidate()
    }
    private val payloadPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun setPayloadColor(value: Int) {
        payloadPaint.color = value
        invalidate()
    }

    private var firstDayOfWeek: Int = Calendar.getInstance().firstDayOfWeek
    fun setFirstDayOfWeek(value: Int) {
        check(value in Calendar.SUNDAY..Calendar.SATURDAY)
        firstDayOfWeek = value
        invalidate()
    }

    private var isEmptyWeeksSkipped: Boolean = true
    fun toSkipEmptyWeeks(value: Boolean) {
        isEmptyWeeksSkipped = value
        val old = yearWeekCurrent
        val containsCurrent = old != null && getWeeks(
            payload = payload,
            firstDayOfWeek = firstDayOfWeek,
            isEmptyWeeksSkipped = value,
            isEmptyTodayWeekSkipped = isEmptyTodayWeekSkipped
        ).any { (year, weeks) ->
            weeks.any { weekOfYear ->
                old.year == year && old.weekOfYear == weekOfYear
            }
        }
        if (!containsCurrent) {
            yearWeekCurrent = getYearWeekDefault(
                payload = payload,
                firstDayOfWeek = firstDayOfWeek,
                isEmptyWeeksSkipped = value,
                isEmptyTodayWeekSkipped = isEmptyTodayWeekSkipped
            )
        }
        invalidate()
    }
    private var isEmptyTodayWeekSkipped: Boolean = true
    fun toSkipEmptyTodayWeek(value: Boolean) {
        isEmptyTodayWeekSkipped = value
        val old = yearWeekCurrent
        val containsCurrent = old != null && getWeeks(
            payload = payload,
            firstDayOfWeek = firstDayOfWeek,
            isEmptyWeeksSkipped = isEmptyWeeksSkipped,
            isEmptyTodayWeekSkipped = value
        ).any { (year, weeks) ->
            weeks.any { weekOfYear ->
                old.year == year && old.weekOfYear == weekOfYear
            }
        }
        if (!containsCurrent) {
            yearWeekCurrent = getYearWeekDefault(
                payload = payload,
                firstDayOfWeek = firstDayOfWeek,
                isEmptyWeeksSkipped = value,
                isEmptyTodayWeekSkipped = isEmptyTodayWeekSkipped
            )
        }
        invalidate()
    }

    private var isAutoSelectToday = false
    fun toSelectTodayAuto(value: Boolean) {
        isAutoSelectToday = value
        invalidate()
    }

    var onWeekChange: (year: Int, weekOfYear: Int) -> Unit = { _, _ -> } // todo

    private var isSelectedDateChanged: Boolean = false
    fun toChangeSelectedDate(value: Boolean) {
        isSelectedDateChanged = value
        invalidate()
    }
    var onSelectDate: (year: Int, month: Int, dayOfMonth: Int) -> Unit = { _, _, _ -> } // todo

    private var dateSelected: YearMonthDay? = null
    private var yearWeekCurrent: YearWeek? = null
    private var xOffset = 0f
    private var startedTrackingXOffset = 0f
    private var animatorX: ObjectAnimator? = null

    private fun startAnimateXOffset(to: Float) {
        animatorX?.cancel()
        animatorX = ObjectAnimator.ofFloat(this, "xOffset", xOffset, to)
            .setDuration(250)
            .also { it.start() }
    }
    // todo proguard
    private fun setXOffset(value: Float) {
        xOffset = value
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var height = dayHeight
        if (isDayNameDrawn) {
            height += dayNameMargin + dayNameHeight
        }
        if (isPayloadDrawn) {
            height += payloadMargin + payloadHeight
        }
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, paddingTop + height.toInt() + paddingBottom)
    }

    private fun getPrevious(current: YearWeek): YearWeek? {
        val yearsToWeeks = getWeeks(
            payload = payload,
            firstDayOfWeek = firstDayOfWeek,
            isEmptyWeeksSkipped = isEmptyWeeksSkipped,
            isEmptyTodayWeekSkipped = isEmptyTodayWeekSkipped
        )
        val years = yearsToWeeks.keys.toList()
        for (y in years.indices) {
            val year = years[y]
            val weeks = yearsToWeeks[year]?.toList() ?: continue
            for (w in weeks.indices) {
                val weekOfYear = weeks[w]
                if (year == current.year && weekOfYear == current.weekOfYear) {
                    if (w > 0) {
                        return YearWeek(year = year, weekOfYear = weeks[w - 1])
                    } else if (y > 0) {
                        val yearPrevious = years[y - 1]
                        val weekPrevious = yearsToWeeks[yearPrevious]?.toList()?.lastOrNull() ?: return null
                        return YearWeek(year = yearPrevious, weekOfYear = weekPrevious)
                    }
                    return null
                }
            }
        }
        return null
    }
    private fun getNext(current: YearWeek): YearWeek? {
        val yearsToWeeks = getWeeks(
            payload = payload,
            firstDayOfWeek = firstDayOfWeek,
            isEmptyWeeksSkipped = isEmptyWeeksSkipped,
            isEmptyTodayWeekSkipped = isEmptyTodayWeekSkipped
        )
        val years = yearsToWeeks.keys.toList()
        for (y in years.indices) {
            val year = years[y]
            val weeks = yearsToWeeks[year]?.toList() ?: continue
            for (w in weeks.indices) {
                val weekOfYear = weeks[w]
                if (year == current.year && weekOfYear == current.weekOfYear) {
                    if (w < weeks.size - 1) {
                        return YearWeek(year = year, weekOfYear = weeks[w + 1])
                    } else if (y < years.size - 1) {
                        val yearNext = years[y + 1]
                        val monthNext = yearsToWeeks[yearNext]?.toList()?.firstOrNull() ?: return null
                        return YearWeek(year = yearNext, weekOfYear = monthNext)
                    }
                    return null
                }
            }
        }
        return null
    }

    private fun onDrawWeek(canvas: Canvas, calendar: Calendar, xOffset: Float) {
        val cellWidth = width.toFloat() / DateUtil.DAYS_IN_WEEK
        val dayTextHeight = dayPaint.getTextHeight("0123456789")
        val dayNameTextHeight = dayNamePaint.getTextHeight(
            (Calendar.SUNDAY..Calendar.SATURDAY).joinToString(separator = "") { dayOfWeekToString(it) }
        )
        for (dayOfWeekNumber in 0 until DateUtil.DAYS_IN_WEEK) {
            var y = paddingTop.toFloat()
            calendar[Calendar.DAY_OF_WEEK] = firstDayOfWeek + dayOfWeekNumber
            val year = calendar[Calendar.YEAR]
            val month = calendar[Calendar.MONTH]
            val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
            val w = xOffset + cellWidth * dayOfWeekNumber
            if (isDayNameDrawn) {
                val value = dayOfWeekToString(calendar[Calendar.DAY_OF_WEEK])
                val textWidth = dayNamePaint.measureText(value)
                canvas.drawText(value, w + cellWidth/2 - textWidth/2, y + dayNameHeight/2 + dayNameTextHeight/2, dayNamePaint)
                y += dayNameHeight + dayNameMargin
            }
            val value = dayOfMonth.toString()
            val isToday = DateUtil.isToday(
                year = year,
                month = month,
                dayOfMonth = dayOfMonth
            )
            val isSelected = DateUtil.isSelected(
                year = year,
                month = month,
                dayOfMonth = dayOfMonth,
                dateSelected = dateSelected,
                isAutoSelectToday = isAutoSelectToday,
                isToday = isToday
            )
            when {
                isSelected -> {
                    daySelectedPaint.color = when {
                        isToday -> daySelectedColorToday
                        else -> daySelectedColorRegular
                    }
                    dayPaint.color = dayTextColorSelected
                    canvas.drawCircle(
                        w + cellWidth / 2,
                        y + dayHeight / 2,
                        dayHeight / 2,
                        daySelectedPaint
                    )
                }
                isToday -> {
                    dayPaint.color = dayTextColorToday
                }
                else -> {
                    dayPaint.color = dayTextColorRegular
                }
            }
            val isWeekendDay = DateUtil.isWeekendDay(firstDayOfWeek = firstDayOfWeek, dayOfWeek = calendar[Calendar.DAY_OF_WEEK])
            if (isWeekendDay) {
                dayPaint.typeface = dayTypefaceWeekend
            } else {
                dayPaint.typeface = dayTypefaceRegular
            }
            val textWidth = dayPaint.measureText(value)
            canvas.drawText(value, w + cellWidth/2 - textWidth/2, y + dayHeight/2 + dayTextHeight/2, dayPaint)
            if (isPayloadDrawn) {
                val payloadText = payload.getData(year = year, month = month, dayOfMonth = dayOfMonth)
                if (!payloadText.isNullOrEmpty()) {
                    y += dayHeight + payloadMargin
                    canvas.drawCircle(w + cellWidth / 2, y + payloadHeight / 2, payloadHeight / 2, payloadPaint)
                }
            }
        }
    }
    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        val current = yearWeekCurrent ?: return
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = firstDayOfWeek
        calendar[Calendar.YEAR] = current.year
        calendar[Calendar.WEEK_OF_YEAR] = current.weekOfYear
        onDrawWeek(canvas, calendar, xOffset = xOffset)
        if (xOffset > 0) {
            val previous = getPrevious(current)
            if (previous != null) {
                calendar[Calendar.YEAR] = previous.year
                calendar[Calendar.WEEK_OF_YEAR] = previous.weekOfYear
                onDrawWeek(canvas, calendar, xOffset = xOffset - width)
            }
        } else if (xOffset < 0) {
            val next = getNext(current)
            if (next != null) {
                calendar[Calendar.YEAR] = next.year
                calendar[Calendar.WEEK_OF_YEAR] = next.weekOfYear
                onDrawWeek(canvas, calendar, xOffset = xOffset + width)
            }
        }
    }

    private fun onTouchDown(x: Float): Boolean {
        startedTrackingXOffset = xOffset - x
        return true
    }
    private fun onTouchMove(x: Float): Boolean {
        val dX = xOffset - startedTrackingXOffset - x
        if (!isMoveStarted) {
            if (dX.absoluteValue < 5.5) return false
            isMoveStarted = true
        }
        xOffset = startedTrackingXOffset + x
        val current = requireNotNull(yearWeekCurrent)
        if (xOffset > width / 2) {
            val previous = getPrevious(current)
            if (previous != null) {
                yearWeekCurrent = previous
                onWeekChange(previous.year, previous.weekOfYear)
                xOffset -= width
                startedTrackingXOffset = xOffset - x
            }
        } else if (xOffset < width / -2) {
            val next = getNext(current)
            if(next != null) {
                yearWeekCurrent = next
                onWeekChange(next.year, next.weekOfYear)
                xOffset += width
                startedTrackingXOffset = xOffset - x
            }
        }
        invalidate()
        return true
    }
    private var isMoveStarted = false
    private fun onTouchUp(x: Float, y: Float): Boolean {
        if (xOffset != 0f) {
            startAnimateXOffset(0f)
        }
        if (isMoveStarted) {
            isMoveStarted = false
            return true
        }
        val yTop = paddingTop.toFloat() + if (isDayNameDrawn) dayNameHeight + dayNameMargin else 0f
        if (y in yTop..(yTop + dayHeight)) {
            val dayWidth = width.toFloat() / DateUtil.DAYS_IN_WEEK
            val dayOfWeekNumber: Int = (x / dayWidth).toInt()
            val current = requireNotNull(yearWeekCurrent)
            val calendar = Calendar.getInstance()
            calendar.firstDayOfWeek = firstDayOfWeek
            calendar[Calendar.YEAR] = current.year
            calendar[Calendar.WEEK_OF_YEAR] = current.weekOfYear
            calendar[Calendar.DAY_OF_WEEK] = firstDayOfWeek + dayOfWeekNumber
            val result = calendar.toYearMonthDay()
            if (isSelectedDateChanged) {
                dateSelected = result
            }
            invalidate()
            onSelectDate(result.year, result.month, result.dayOfMonth)
        }
        return true
    }
    private fun onTouchEventSingle(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> onTouchDown(x = event.x)
            MotionEvent.ACTION_MOVE -> onTouchMove(x = event.x)
            MotionEvent.ACTION_UP -> onTouchUp(x = event.x, y = event.y)
            else -> false
        }
    }
    private fun onTouchEventMulti(event: MotionEvent): Boolean {
        return when(event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> onTouchDown(event.x)
            MotionEvent.ACTION_MOVE -> onTouchMove(event.x)
            MotionEvent.ACTION_POINTER_UP -> {
                val x0 = event.getX(0)
                val x1 = event.getX(1)
                when(event.actionIndex) {
                    0 -> onTouchDown(x1)
                    else -> onTouchDown(x0)
                }
            }
            else -> false
        }
    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        return when(event.pointerCount) {
            1 -> onTouchEventSingle(event)
            else -> onTouchEventMulti(event)
        }
    }
}
