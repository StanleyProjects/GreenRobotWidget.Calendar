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
import java.util.TimeZone
import kotlin.math.absoluteValue
import sp.grw.calendar.entity.ActiveType
import sp.grw.calendar.entity.Payload
import sp.grw.calendar.entity.YearMonth
import sp.grw.calendar.entity.YearMonthDay
import sp.grw.calendar.entity.isPresent
import sp.grw.calendar.util.DateUtil
import sp.grw.calendar.util.DateUtil.toYearMonth
import sp.grw.calendar.util.DateUtil.toYearMonthDay
import sp.grw.calendar.util.AndroidUtil.getTextHeight
import sp.grw.calendar.util.AndroidUtil.drawRoundRect

class MonthScrollerView(context: Context) : View(context) {
    enum class LineTypeHorizontal {
        NONE, REGULAR, CLIPPED
    }

    companion object {
        /**
         * @return Map<Int(Year), Set<Int(Month)>>
        */
        private fun allMonths(
            minYear: Int,
            minMonth: Int,
            maxYear: Int,
            maxMonth: Int
        ): Map<Int, Set<Int>> {
            if (minYear == maxYear) return mapOf(minYear to (minMonth..maxMonth).toSet())
            val result = mutableMapOf<Int, Set<Int>>()
            result[minYear] = (minMonth..11).toSet()
            for (year in (minYear + 1) until maxYear) {
                result[year] = (0..11).toSet()
            }
            result[maxYear] = (0..maxMonth).toSet()
            return result
        }

        /**
         * @return Map<Int(Year), Set<Int(Month)>>
         */
        private fun allMonths(
            payload: Payload,
            firstDayOfWeek: Int,
            timeZone: TimeZone,
            isEmptyTodayMonthSkipped: Boolean
        ): Map<Int, Set<Int>> {
            if (payload.isEmpty()) {
                return if (isEmptyTodayMonthSkipped) emptyMap()
                else {
                    val calendar = DateUtil.calendar(
                        firstDayOfWeek = firstDayOfWeek,
                        timeZone = timeZone
                    )
                    mapOf(calendar[Calendar.YEAR] to setOf(calendar[Calendar.MONTH]))
                }
            }
            val years = payload.getYears()
            val minYear = years.min()!!
            val minMonth = payload.getMonths(year = minYear).min()!!
            val maxYear = years.max()!!
            val maxMonth = payload.getMonths(year = maxYear).max()!!
            if (isEmptyTodayMonthSkipped) {
                return allMonths(
                    minYear = minYear,
                    minMonth = maxMonth,
                    maxYear = maxYear,
                    maxMonth = maxMonth
                )
            }
            val today = DateUtil.calendar(
                firstDayOfWeek = firstDayOfWeek,
                timeZone = timeZone
            ).toYearMonth()
            val min = when {
                minYear > today.year -> today
                minYear == today.year -> YearMonth(year = minYear, month = kotlin.math.min(minMonth, today.month))
                else -> YearMonth(year = minYear, month = minMonth)
            }
            val max = when {
                maxYear < today.year -> today
                maxYear == today.year -> YearMonth(year = maxYear, month = kotlin.math.max(maxMonth, today.month))
                else -> YearMonth(year = maxYear, month = maxMonth)
            }
            return allMonths(
                minYear = min.year,
                minMonth = min.month,
                maxYear = max.year,
                maxMonth = max.month
            )
        }

        /**
         * @return Map<Int(Year), Set<Int(Month)>>
         */
        private fun onlyMonthsWithDays(
            payload: Payload,
            firstDayOfWeek: Int,
            timeZone: TimeZone,
            isTodayMonthIfEmptySkipped: Boolean
        ): Map<Int, Set<Int>> {
            val result = mutableMapOf<Int, Set<Int>>()
            payload.forEachMonths { year, months ->
                result[year] = months
            }
            if (!isTodayMonthIfEmptySkipped) {
                val calendar = DateUtil.calendar(
                    firstDayOfWeek = firstDayOfWeek,
                    timeZone = timeZone
                )
                val months = result[calendar[Calendar.YEAR]].orEmpty() + calendar[Calendar.MONTH]
                result[calendar[Calendar.YEAR]] = months.sortedBy { it }.toSet()
            }
            return result
        }

        /**
         * @return Map<Int(Year), Set<Int(Month)>>
         */
        private fun getMonths(
            payload: Payload,
            firstDayOfWeek: Int,
            timeZone: TimeZone,
            isEmptyMonthsSkipped: Boolean,
            isEmptyTodayMonthSkipped: Boolean
        ): Map<Int, Set<Int>> {
            return if (isEmptyMonthsSkipped) {
                onlyMonthsWithDays(
                    payload = payload,
                    firstDayOfWeek = firstDayOfWeek,
                    timeZone = timeZone,
                    isTodayMonthIfEmptySkipped = isEmptyTodayMonthSkipped
                )
            } else {
                allMonths(
                    payload = payload,
                    firstDayOfWeek = firstDayOfWeek,
                    timeZone = timeZone,
                    isEmptyTodayMonthSkipped = isEmptyTodayMonthSkipped
                )
            }
        }

        private fun getYearMonthDefault(
            payload: Payload,
            firstDayOfWeek: Int,
            timeZone: TimeZone,
            isEmptyMonthsSkipped: Boolean,
            isEmptyTodayMonthSkipped: Boolean
        ): YearMonth? {
            val today = DateUtil.calendar(
                firstDayOfWeek = firstDayOfWeek,
                timeZone = timeZone
            ).toYearMonth()
            if (!isEmptyTodayMonthSkipped) return today
            val months = getMonths(
                payload = payload,
                firstDayOfWeek = firstDayOfWeek,
                timeZone = timeZone,
                isEmptyMonthsSkipped = isEmptyMonthsSkipped,
                isEmptyTodayMonthSkipped = isEmptyTodayMonthSkipped
            )
            val ms = months[today.year]
            if (ms != null && ms.any { it == today.month }) return today
            val year = months.keys.firstOrNull() ?: return null
            val month = months[year]?.firstOrNull() ?: return null
            return YearMonth(year = year, month = month)
        }
    }

    private var dayHeight = 0f
    fun setDayHeight(value: Float) {
        if (value < 0) error("Negative height!")
        dayHeight = value
        invalidate()
    }
    private var dayPaddingTop = 0f
    fun setDayPaddingTop(value: Float) {
        if (value < 0) error("Negative padding!")
        dayPaddingTop = value
        invalidate()
    }
    private var dayPaddingBottom = 0f
    fun setDayPaddingBottom(value: Float) {
        if (value < 0) error("Negative padding!")
        dayPaddingBottom = value
        invalidate()
    }
    private val dayPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun setDayTextSize(value: Float) {
        dayPaint.textSize = value
        invalidate()
    }
    private var dayTextColorRegular = Color.BLACK
    fun setDayTextColorRegular(value: Int) {
        dayTextColorRegular = value
        invalidate()
    }
    private var dayTextColorNotActive = Color.GRAY
    fun setDayTextColorNotActive(value: Int) {
        dayTextColorNotActive = value
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

    private var payload: Payload = Payload(emptyMap())
    fun setPayload(value: Map<Int, Map<Int, Map<Int, String>>>) {
        payload = Payload(value)
        yearMonthCurrent = getYearMonthDefault(
            payload = payload,
            firstDayOfWeek = firstDayOfWeek,
            timeZone = timeZone,
            isEmptyMonthsSkipped = isEmptyMonthsSkipped,
            isEmptyTodayMonthSkipped = isEmptyTodayMonthSkipped
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
    fun setPayloadTextSize(value: Float) {
        payloadPaint.textSize = value
        invalidate()
    }
    fun setPayloadTextColor(value: Int) {
        payloadPaint.color = value
        invalidate()
    }
    private val payloadBackgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun setPayloadBackgroundColor(value: Int) {
        payloadBackgroundPaint.color = value
        invalidate()
    }
    private var isPayloadEmptySelectable: Boolean = false
    fun toSelectPayloadEmpty(value: Boolean) {
        isPayloadEmptySelectable = value
        invalidate()
    }

    private var isEmptyMonthsSkipped: Boolean = true
    fun toSkipEmptyMonths(value: Boolean) {
        isEmptyMonthsSkipped = value
        val old = yearMonthCurrent
        val containsCurrent = old != null && getMonths().any { (year, months) ->
            months.any { month ->
                old.year == year && old.month == month
            }
        }
        if (!containsCurrent) {
	        yearMonthCurrent = getYearMonthDefault(
	            payload = payload,
                firstDayOfWeek = firstDayOfWeek,
                timeZone = timeZone,
	            isEmptyMonthsSkipped = value,
	            isEmptyTodayMonthSkipped = isEmptyTodayMonthSkipped
	        )
        }
        invalidate()
    }
    private var isEmptyTodayMonthSkipped: Boolean = true
    fun toSkipEmptyTodayMonth(value: Boolean) {
        isEmptyTodayMonthSkipped = value
        val old = yearMonthCurrent
        val containsCurrent = old != null && getMonths().any { (year, months) ->
            months.any { month ->
                old.year == year && old.month == month
            }
        }
        if (!containsCurrent) {
	        yearMonthCurrent = getYearMonthDefault(
	            payload = payload,
                firstDayOfWeek = firstDayOfWeek,
                timeZone = timeZone,
	            isEmptyMonthsSkipped = isEmptyMonthsSkipped,
	            isEmptyTodayMonthSkipped = value
	        )
        }
        invalidate()
    }

    private var firstDayOfWeek: Int = Calendar.getInstance().firstDayOfWeek
    fun setFirstDayOfWeek(value: Int) {
        check(value in Calendar.SUNDAY..Calendar.SATURDAY)
        firstDayOfWeek = value
        invalidate()
    }
    private var timeZone: TimeZone = Calendar.getInstance().timeZone
    fun setTimeZone(value: TimeZone) {
        timeZone = value
        invalidate()
    }

    private var lineTypeHorizontal: LineTypeHorizontal = LineTypeHorizontal.NONE
    fun setLineTypeHorizontal(value: LineTypeHorizontal) {
        lineTypeHorizontal = value
        invalidate()
    }
    private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun setLineColor(value: Int) {
        linePaint.color = value
        invalidate()
    }
    fun setLineSize(value: Float) {
        linePaint.strokeWidth = value
        invalidate()
    }

    private var isAutoSelectToday = false
    fun toSelectTodayAuto(value: Boolean) {
        isAutoSelectToday = value
        invalidate()
    }

    private var nonActiveAlpha: Int = 255
    fun setNonActiveAlpha(value: Int) {
        check(value in 0..255)
        nonActiveAlpha = value
        invalidate()
    }

    var onMonthChange: (year: Int, month: Int) -> Unit = { _, _ -> } // todo

    private var isSelectedDateChanged: Boolean = false
    fun toChangeSelectedDate(value: Boolean) {
        isSelectedDateChanged = value
        invalidate()
    }
    var onSelectDate: (year: Int, month: Int, dayOfMonth: Int) -> Unit = { _, _, _ -> } // todo

    private var dateSelected: YearMonthDay? = null
    private var yearMonthCurrent: YearMonth? = null
    private var cellWidth = 0f
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

    private fun getCellHeight(): Float {
        return if (isPayloadDrawn) {
            dayPaddingTop + dayHeight + payloadMargin + payloadHeight + dayPaddingBottom
        } else {
            dayPaddingTop + dayHeight + dayPaddingBottom
        }
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = getCellHeight() * 6 // todo
        val width = MeasureSpec.getSize(widthMeasureSpec)
        cellWidth = width.toFloat() / DateUtil.DAYS_IN_WEEK
        setMeasuredDimension(width, height.toInt())
    }

    /**
     * @return Map<Int(Year), Set<Int(Month)>>
     */
    private fun getMonths(): Map<Int, Set<Int>> {
        return getMonths(
            payload = payload,
            firstDayOfWeek = firstDayOfWeek,
            timeZone = timeZone,
            isEmptyMonthsSkipped = isEmptyMonthsSkipped,
            isEmptyTodayMonthSkipped = isEmptyTodayMonthSkipped
        )
    }

    private fun getPrevious(current: YearMonth): YearMonth? {
        val yearsToMonths = getMonths()
        val years = yearsToMonths.keys.toList()
        for (y in years.indices) {
            val year = years[y]
            val months = yearsToMonths[year]?.toList() ?: continue
            for (m in months.indices) {
                val month = months[m]
                if (year == current.year && month == current.month) {
                    if (m > 0) {
                        return YearMonth(year = year, month = months[m - 1])
                    } else if (y > 0) {
                        val yearPrevious = years[y - 1]
                        val monthPrevious = yearsToMonths[yearPrevious]?.toList()?.lastOrNull() ?: return null
                        return YearMonth(year = yearPrevious, month = monthPrevious)
                    }
                    return null
                }
            }
        }
        return null
    }
    private fun getNext(current: YearMonth): YearMonth? {
        val yearsToMonths = getMonths()
        val years = yearsToMonths.keys.toList()
        for (y in years.indices) {
            val year = years[y]
            val months = yearsToMonths[year]?.toList() ?: continue
            for (m in months.indices) {
                val month = months[m]
                if (year == current.year && month == current.month) {
                    if (m < months.size - 1) {
                        return YearMonth(year = year, month = months[m + 1])
                    } else if (y < years.size - 1) {
                        val yearNext = years[y + 1]
                        val monthNext = yearsToMonths[yearNext]?.toList()?.firstOrNull() ?: return null
                        return YearMonth(year = yearNext, month = monthNext)
                    }
                    return null
                }
            }
        }
        return null
    }

    private var activeType: ActiveType = ActiveType.FUTURE
    fun setActiveType(value: ActiveType) {
        activeType = value
        invalidate()
    }
    private fun isActive(year: Int, month: Int, dayOfMonth: Int): Boolean {
        return when (activeType) {
            ActiveType.ALL -> true
            ActiveType.PAYLOAD -> payload.isPresent(year = year, month = month, dayOfMonth = dayOfMonth)
            ActiveType.FUTURE -> !DateUtil.isBeforeToday(
                firstDayOfWeek = firstDayOfWeek,
                timeZone = timeZone,
                year = year,
                month = month,
                dayOfMonth = dayOfMonth
            )
        }
    }

    private fun onDrawDay(
        canvas: Canvas,
        xOffset: Float,
        cellWidth: Float,
        cellHeight: Float,
        weekOfMonth: Int,
        firstDayOfWeek: Int,
        dayOfWeekNumber: Int,
        dayOfMonth: Int,
        payload: String?,
        isActive: Boolean,
        isToday: Boolean,
        isSelected: Boolean
    ) {
        val dayText = dayOfMonth.toString()
        val dayTextWidth = dayPaint.measureText(dayText)
        val dayTextHeight = dayPaint.getTextHeight(dayText)
        dayPaint.color = when {
            isSelected -> dayTextColorSelected
            isToday -> dayTextColorToday
            isActive -> dayTextColorRegular
            else -> dayTextColorNotActive
        }
        val alpha: Int
        if (isActive) {
            alpha = 255
        } else {
            alpha = nonActiveAlpha
        }
        payloadPaint.alpha = alpha
        payloadBackgroundPaint.alpha = alpha
        val dayOfWeek = (firstDayOfWeek + dayOfWeekNumber).let {
            if (it > 7) it - 7 else it
        }
        val isWeekendDay = DateUtil.isWeekendDay(firstDayOfWeek = firstDayOfWeek, dayOfWeek = dayOfWeek)
        if (isWeekendDay) {
            dayPaint.typeface = dayTypefaceWeekend
        } else {
            dayPaint.typeface = dayTypefaceRegular
        }
        val x = xOffset + cellWidth * dayOfWeekNumber
        val y = cellHeight * weekOfMonth
        if (isSelected) {
            daySelectedPaint.color = when {
                isToday -> daySelectedColorToday
                else -> daySelectedColorRegular
            }
            canvas.drawCircle(
                x + cellWidth / 2,
                y + dayPaddingTop + dayHeight / 2,
                dayHeight / 2,
                daySelectedPaint
            )
        }
        canvas.drawText(
            dayText,
            x + cellWidth / 2 - dayTextWidth / 2,
            y + dayPaddingTop + dayHeight / 2 + dayTextHeight / 2,
            dayPaint
        )
        val dayTextHeightFull = dayPaddingTop + dayHeight
        if (!isPayloadDrawn || payload.isNullOrEmpty()) return
        val payloadY = y + dayTextHeightFull + payloadMargin
        val payloadTextWidth = payloadPaint.measureText(payload)
        val payloadTextHeight = payloadPaint.getTextHeight("0123456789")
        val r = payloadHeight / 2 // todo
        canvas.drawRoundRect(
            left = x + (cellWidth - payloadTextWidth) / 2 - r,
            top = payloadY,
            right = x + cellWidth - (cellWidth - payloadTextWidth) / 2 + r,
            bottom = payloadY + payloadHeight,
            radius = r,
            paint = payloadBackgroundPaint
        )
        canvas.drawText(
            payload,
            x + cellWidth / 2 - payloadTextWidth / 2,
            payloadY + payloadHeight / 2 + payloadTextHeight / 2,
            payloadPaint
        )
    }
    private fun onDrawMonth(
        canvas: Canvas,
        xOffset: Float,
        yearTarget: Int,
        monthTarget: Int
    ) {
        val calendar = DateUtil.calendar(
            firstDayOfWeek = firstDayOfWeek,
            timeZone = timeZone
        )
        val today = calendar.toYearMonthDay()
        val weeksInMonth = DateUtil.calculateWeeksInMonth(
            year = yearTarget,
            month = monthTarget,
            firstDayOfWeek = firstDayOfWeek
        )
        for (weekOfMonth in 0 until weeksInMonth) {
            calendar[Calendar.YEAR] = yearTarget
            calendar[Calendar.MONTH] = monthTarget
            calendar[Calendar.DAY_OF_MONTH] = 1
            val weekOfYear = calendar[Calendar.WEEK_OF_YEAR]
            calendar[Calendar.WEEK_OF_YEAR] = weekOfYear + weekOfMonth
            val cellHeight = getCellHeight()
            for (dayOfWeekNumber in 0 until DateUtil.DAYS_IN_WEEK) {
                calendar[Calendar.DAY_OF_WEEK] = firstDayOfWeek + dayOfWeekNumber
                val year = calendar[Calendar.YEAR]
                val month = calendar[Calendar.MONTH]
                if (year != yearTarget || month != monthTarget) continue // todo test
                val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
                val isToday = today.year == year && today.month == month && today.dayOfMonth == dayOfMonth
                val isSelected = DateUtil.isSelected(
                    year = year,
                    month = month,
                    dayOfMonth = dayOfMonth,
                    dateSelected = dateSelected,
                    isAutoSelectToday = isAutoSelectToday,
                    isToday = isToday
                )
                val isActive = isActive(year = year, month = month, dayOfMonth = dayOfMonth)
                onDrawDay(
                    canvas = canvas,
                    xOffset = xOffset,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    weekOfMonth = weekOfMonth,
                    firstDayOfWeek = firstDayOfWeek,
                    dayOfWeekNumber = dayOfWeekNumber,
                    dayOfMonth = dayOfMonth,
                    payload = payload.getData(year = year, month = month, dayOfMonth = dayOfMonth),
                    isActive = isActive,
                    isToday = isToday,
                    isSelected = isSelected
                )
            }
            when (lineTypeHorizontal) {
                LineTypeHorizontal.REGULAR -> {
                    val yLine = cellHeight * (weekOfMonth + 1) - linePaint.strokeWidth / 2
                    canvas.drawLine(
                        xOffset,
                        yLine,
                        xOffset + width.toFloat(),
                        yLine,
                        linePaint
                    )
                }
                LineTypeHorizontal.CLIPPED -> {
                    TODO()
                }
	        }
        }
        // todo line vertical
    }
    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        val current = yearMonthCurrent ?: return
        onDrawMonth(
            canvas = canvas,
            xOffset = xOffset,
            yearTarget = current.year,
            monthTarget = current.month
        )
        if (xOffset > 0) {
            val previous = getPrevious(current)
            if (previous != null) {
                onDrawMonth(
                    canvas = canvas,
                    xOffset = xOffset - width,
                    yearTarget = previous.year,
                    monthTarget = previous.month
                )
            }
        } else if (xOffset < 0) {
            val next = getNext(current)
            if (next != null) {
                onDrawMonth(
                    canvas = canvas,
                    xOffset = xOffset + width,
                    yearTarget = next.year,
                    monthTarget = next.month
                )
            }
        }
    }

    private fun onTouchDown(x: Float): Boolean {
        startedTrackingXOffset = xOffset - x
        return true
    }
    private var isMoveStarted = false
    private fun onTouchMove(x: Float): Boolean {
        val dX = xOffset - startedTrackingXOffset - x
        if (!isMoveStarted) {
            if (dX.absoluteValue < 5.5) return false
            isMoveStarted = true
        }
        val current = requireNotNull(yearMonthCurrent)
        xOffset = startedTrackingXOffset + x
        if(xOffset > width / 2) {
            val previous = getPrevious(current)
            if (previous != null) {
                yearMonthCurrent = previous
                onMonthChange(previous.year, previous.month)
                xOffset -= width
                startedTrackingXOffset = xOffset - x
            }
        } else if(xOffset < width / -2) {
            val next = getNext(current)
            if(next != null) {
                yearMonthCurrent = next
                onMonthChange(next.year, next.month)
                xOffset += width
                startedTrackingXOffset = xOffset - x
            }
        }
        invalidate()
        return true
    }
    private fun onTouchUp(x: Float, y: Float): Boolean {
        if (xOffset != 0f) {
            startAnimateXOffset(0f)
        }
        if (isMoveStarted) {
            isMoveStarted = false
            return true
        }
        val current = requireNotNull(yearMonthCurrent)
        val weeksInMonth = DateUtil.calculateWeeksInMonth(year = current.year, month = current.month, firstDayOfWeek = firstDayOfWeek)
        val cellHeight = getCellHeight()
        if (y > weeksInMonth * cellHeight) return true
        val weekNumber: Int = (y / cellHeight).toInt()
        val dayOfWeekNumber: Int = (x / cellWidth).toInt()
        val calendar = DateUtil.calendar(
            firstDayOfWeek = firstDayOfWeek,
            timeZone = timeZone
        )
        calendar[Calendar.YEAR] = current.year
        calendar[Calendar.MONTH] = current.month
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.WEEK_OF_YEAR] = calendar[Calendar.WEEK_OF_YEAR] + weekNumber
        calendar[Calendar.DAY_OF_WEEK] = firstDayOfWeek + dayOfWeekNumber
        val result = calendar.toYearMonthDay()
        if (current.year == result.year && current.month == result.month) {
            val payload = payload.getData(year = result.year, month = result.month, dayOfMonth = result.dayOfMonth)
            if (isPayloadEmptySelectable || payload != null) {
                val isSelected = DateUtil.isSelected(
                    year = result.year,
                    month = result.month,
                    dayOfMonth = result.dayOfMonth,
                    dateSelected = dateSelected,
                    isAutoSelectToday = isAutoSelectToday
                )
                if (!isSelected) {
                    if (isSelectedDateChanged) {
                        dateSelected = result
                        invalidate()
                    }
                }
                onSelectDate(result.year, result.month, result.dayOfMonth)
            }
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
