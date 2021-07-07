package sp.grw.calendar

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.absoluteValue
import sp.grw.calendar.entity.YearMonthDay
import sp.grw.calendar.util.AndroidUtil.appendArc
import sp.grw.calendar.util.AndroidUtil.drawRoundRect
import sp.grw.calendar.util.AndroidUtil.getTextHeight
import sp.grw.calendar.util.DateUtil
import sp.grw.calendar.util.DateUtil.toYearMonthDay

class ScheduleView(context: Context) : View(context) {
    private class Item(val start: Int, val end: Int, val payload: String) {
        override fun toString(): String {
            return "{$start..$end/$payload}"
        }
    }
    private enum class TimeType { START, END }

    companion object {
        private fun toMatrix(groups: List<List<Item>>, ladderSize: Int): Array<Array<List<Item>>> {
            val result = mutableListOf<MutableList<List<Item>>>()
            repeat(ladderSize) {
                result.add(mutableListOf())
            }
            val dates = groups.map { group ->
                listOf(
                    group to TimeType.START,
                    group to TimeType.END
                )
            }.flatten().sortedBy { (group, timeType) ->
                when (timeType) {
                    TimeType.START -> group.minOfOrNull { it.start }!!
                    TimeType.END -> group.maxOfOrNull { it.end }!!
                }
            }
            val stack = mutableListOf<List<Item>>()
            for (indexDate in dates.indices) {
                val (group, timeType) = dates[indexDate]
                if (timeType == TimeType.START) {
                    result[stack.size].add(group)
                    stack.add(group)
                    continue
                }
                if (stack.isEmpty()) continue
                if (stack.last() != group) continue
                while (stack.isNotEmpty()) {
                    val event = group.maxByOrNull { it.end }!!
                    val eventLast = stack.last().maxByOrNull { it.end }!!
                    if (eventLast.end > event.end) break
                    stack.removeAt(stack.lastIndex)
                }
            }
            return result.map { it.toTypedArray() }.toTypedArray()
        }

        private fun group(
            range: List<Pair<Item, TimeType>>,
            levelsToGroup: Int
        ): Iterable<List<Item>> {
            val stackEvent = mutableListOf<Item>()
            val stackGroup = mutableListOf<MutableList<Item>>()
            val result = mutableListOf<List<Item>>()
            for (indexDate in range.indices) {
                val (event, timeType) = range[indexDate]
                if (timeType == TimeType.START) {
                    if (stackEvent.size % levelsToGroup == 0) {
                        stackGroup.add(mutableListOf())
                    }
                    stackGroup.last().add(event)
                    stackEvent.add(event)
                    continue
                }
                if (stackEvent.isEmpty()) continue
                if (stackEvent.last() != event) continue
                while (stackEvent.isNotEmpty()) {
                    if (stackEvent.last().end > event.end) break
                    stackEvent.removeAt(stackEvent.lastIndex)
                    if (stackEvent.size % levelsToGroup == 0) {
                        result.add(stackGroup.last())
                        stackGroup.removeAt(stackGroup.lastIndex)
                    }
                }
            }
            return result
        }

        private fun transform(
            list: List<Triple<Int, Int, String>>,
            timeMinimumInMinutes: Int?,
            ladderSize: Int
        ): List<List<Item>> {
            val dates = list.map { (start, endActual, payload) ->
                val end = if (timeMinimumInMinutes == null) endActual
                else if (endActual - start < timeMinimumInMinutes) start + timeMinimumInMinutes else endActual
                val item = Item(start = start, end = end, payload = payload)
                listOf(
                    item to TimeType.START,
                    item to TimeType.END
                )
            }.flatten()
                .sortedBy { (event, timeType) ->
                    when (timeType) {
                        TimeType.START -> event.start
                        TimeType.END -> event.end
                    }
                }
            val stack = mutableListOf<Item>()
            var currentLadderStart = -1
            var maxLength = -1
            val result = mutableListOf<List<Item>>()
            for (indexDate in dates.indices) {
                val (event, timeType) = dates[indexDate]
                if (timeType == TimeType.START) {
                    if (stack.isEmpty()) {
                        currentLadderStart = indexDate
                        maxLength = 0
                    }
                    stack.add(event)
                    maxLength = kotlin.math.max(maxLength, stack.size)
                    continue
                }
                if (timeType != TimeType.END) TODO()
                if (stack.isEmpty()) continue
                if (event != stack.last()) continue
                while (stack.isNotEmpty()) {
                    if (stack.last().end > event.end) break
                    stack.removeAt(stack.lastIndex)
                }
                if (stack.isNotEmpty()) continue
                val currentLadderEnd = indexDate + 1 // todo
                val range = dates.subList(currentLadderStart, currentLadderEnd)
                if (maxLength > ladderSize) {
                    val levelsToGroup = kotlin.math.ceil(maxLength.toDouble() / ladderSize).toInt()
                    result.addAll(group(range, levelsToGroup))
                } else {
                    result.addAll(range.mapNotNull { (e, tt) ->
                        if (tt == TimeType.START) listOf(e) else null
                    })
                }
            }
            return result
        }
    }

    private var timeRange = IntRange.EMPTY
    fun setTimeRange(start: Int, endInclusive: Int) {
        if (start < 0) error("Time range minimum 0!")
        val max = 24 * 60
        if (endInclusive > max) error("Time range maximum $max!")
        timeRange = IntRange(start, endInclusive)
        invalidate()
    }
    private var timeStepMinutes: Int = 30
    fun setTimeStepMinutes(value: Int) {
        timeStepMinutes = value
        invalidate()
    }
    private var timeStepHeight: Float = 10f
    fun setTimeStepHeight(value: Float) {
        timeStepHeight = value
        invalidate()
    }
    private var timeMark: Int? = null
    private val timeMarkPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun setTimeMarkColor(value: Int) {
        timeMarkPaint.color = value
        invalidate()
    }
    fun setTimeMarkSize(value: Float) {
        timeMarkPaint.strokeWidth = value
        invalidate()
    }
    fun setTimeMarkAlpha(value: Int) {
        timeMarkPaint.alpha = value
        invalidate()
    }
    private var timeMarkRadius: Float = 0f
    fun setTimeMarkRadius(value: Float) {
        timeMarkRadius = value
        invalidate()
    }
    fun setTimeMarkAuto(year: Int, month: Int, dayOfMonth: Int) {
        // todo check
        val calendar = DateUtil.calendar(
            firstDayOfWeek = firstDayOfWeek,
            timeZone = timeZone
        )
        val yearMonthDay = DateUtil.calendar(
            firstDayOfWeek = firstDayOfWeek,
            timeZone = timeZone
        ).also {
            it[Calendar.YEAR] = year
            it[Calendar.MONTH] = month
            it[Calendar.DAY_OF_MONTH] = dayOfMonth
        }.toYearMonthDay()
        this.yearMonthDayCurrent = yearMonthDay
        if (
            calendar[Calendar.YEAR] == yearMonthDay.year &&
            calendar[Calendar.MONTH] == yearMonthDay.month &&
            calendar[Calendar.DAY_OF_MONTH] == yearMonthDay.dayOfMonth
        ) {
            val hourOfDay = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]
            val minutes = hourOfDay * 60 + minute
            if (minutes in timeRange) {
                timeMark = minutes
            } else {
                timeMark = null
            }
        } else {
            timeMark = null
        }
        invalidate()
    }
    private var timeLineMarginStart: Float = 0f
    fun setTimeLineMarginStart(value: Float) {
        timeLineMarginStart = value
        invalidate()
    }
    private var timeLineMarginEnd: Float = 0f
    fun setTimeLineMarginEnd(value: Float) {
        timeLineMarginEnd = value
        invalidate()
    }
    private val timeLinePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun setTimeLineColor(value: Int) {
        timeLinePaint.color = value
        invalidate()
    }
    fun setTimeLineSize(value: Float) {
        timeLinePaint.strokeWidth = value
        invalidate()
    }
    private var timeLineCount: Int = 1
    fun setTimeLineCount(value: Int) {
        if (value < 0) error("Negative value!")
        timeLineCount = value
        invalidate()
    }

    private val timePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun setTimeTextColor(value: Int) {
        timePaint.color = value
        invalidate()
    }
    fun setTimeTextSize(value: Float) {
        timePaint.textSize = value
        invalidate()
    }
    private var timeTextMargin: Float = 0f
    fun setTimeTextMargin(value: Float) {
        timeTextMargin = value
        invalidate()
    }

    private var groupMarginStart: Float = 0f
    fun setGroupMarginStart(value: Float) {
        groupMarginStart = value
        invalidate()
    }
    private var groupMarginEnd: Float = 0f
    fun setGroupMarginEnd(value: Float) {
        groupMarginEnd = value
        invalidate()
    }
    private var groupMargin: Float = 0f
    fun setGroupMargin(value: Float) {
        groupMargin = value
        invalidate()
    }
    private var groupPaddingTop: Float = 0f
    fun setGroupPaddingTop(value: Float) {
        groupPaddingTop = value
        invalidate()
    }
    private var groupPaddingStart: Float = 0f
    fun setGroupPaddingStart(value: Float) {
        groupPaddingStart = value
        invalidate()
    }
    private var groupPaddingEnd: Float = 0f
    fun setGroupPaddingEnd(value: Float) {
        groupPaddingEnd = value
        invalidate()
    }
    private val groupBackgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun setGroupBackgroundColor(value: Int) {
        groupBackgroundPaint.color = value
        invalidate()
    }
    private val groupForegroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    fun setGroupForegroundColor(value: Int) {
        groupForegroundPaint.color = value
        invalidate()
    }
    fun setGroupTextSize(value: Float) {
        groupForegroundPaint.textSize = value
        invalidate()
    }
    private var groupBackgroundAlphaActive: Int = 255
    private var groupBackgroundAlphaNotActive: Int = 255
    fun setGroupBackgroundAlpha(isActive: Boolean, value: Int) {
        check(value in 0..255)
        if (isActive) {
            groupBackgroundAlphaActive = value
        } else {
            groupBackgroundAlphaNotActive = value
        }
        invalidate()
    }
    private var groupForegroundAlphaActive: Int = 255
    private var groupForegroundAlphaNotActive: Int = 255
    fun setGroupForegroundAlpha(isActive: Boolean, value: Int) {
        check(value in 0..255)
        if (isActive) {
            groupForegroundAlphaActive = value
        } else {
            groupForegroundAlphaNotActive = value
        }
        invalidate()
    }
    private var groupRadius: Float = 0f
    fun setGroupRadius(value: Float) {
        groupRadius = value
        invalidate()
    }
    private var groupLineCountMax: Int? = null
    fun setGroupLineCountMax(value: Int) {
        if (value < 0) error("Negative value!")
        groupLineCountMax = value
        invalidate()
    }
    private var groupLineCountMaxByGroupHeight: Boolean = false
    fun setGroupLineCountMaxByGroupHeight(value: Boolean) {
        groupLineCountMaxByGroupHeight = value
        invalidate()
    }

    fun removeGroupLineCountMax() {
        groupLineCountMax = null
        invalidate()
    }
    private var groupTextLineSpace: Float = 0f
    fun setGroupTextLineSpace(value: Float) {
        groupTextLineSpace = value
        invalidate()
    }
    private var groupMinHeight = 0f
    fun setGroupMinHeightInMinutes(value: Int) {
        if (value < 0) error("Negative value!")
        groupMinHeight = (timeStepHeight / timeStepMinutes) * value
        invalidate()
    }

    private var firstDayOfWeek: Int = Calendar.getInstance().firstDayOfWeek
    private var timeZone: TimeZone = Calendar.getInstance().timeZone
    fun setTimeZone(value: TimeZone) {
        timeZone = value
        invalidate()
    }

    private var ladderSize: Int = 4 // todo default
    private var groupMatrix: Array<Array<List<Item>>> = Array(ladderSize) { emptyArray<List<Item>>() }
    fun setPayload(list: List<Triple<Int, Int, String>>, timeMinimumInMinutes: Int?) {
        val events = transform(list, timeMinimumInMinutes = timeMinimumInMinutes, ladderSize = ladderSize)
        groupMatrix = toMatrix(events, ladderSize = ladderSize)
    }

    var onGroupClick: (start: Int, end: Int) -> Unit = { _, _ -> } // todo default

    private var yearMonthDayCurrent: YearMonthDay? = null
    private var yOffset = 0f
    private var startedTrackingYOffset = 0f
    private var animatorY: ObjectAnimator? = null

    private fun startAnimateYOffset(to: Float) {
        animatorY?.cancel()
        animatorY = ObjectAnimator.ofFloat(this, "yOffset", yOffset, to)
            .setDuration(250)
            .also { it.start() }
    }
    // todo proguard
    private fun setYOffset(value: Float) {
        yOffset = value
        invalidate()
    }

    private fun onDrawGroup(canvas: Canvas, ladderSize: Int, isActive: Boolean, group: List<Item>, row: Int) {
        if (group.isEmpty()) return
        if (ladderSize < 1) return
        val groupWidthFull: Float = width - groupMarginStart - groupMarginEnd
        val groupWidth: Float = (groupWidthFull - groupMargin * (ladderSize - 1)) / ladderSize
        val groupX: Float = groupMarginStart + groupWidth * row + groupMargin * row
        val start = group.minOfOrNull { it.start }!!
        val dY = timeStepHeight / timeStepMinutes
        val startingPointY = yOffset + paddingTop
        val yStart = startingPointY + start * dY - timeRange.start * dY
        val end = group.maxOfOrNull { it.end }!!
        val dHeight = startingPointY + end * dY - timeRange.start * dY - yStart
        val yEnd = yStart + kotlin.math.max(dHeight, groupMinHeight)
        val groupHeight = yEnd - yStart
        if (isActive) {
            groupBackgroundPaint.alpha = groupBackgroundAlphaActive
            groupForegroundPaint.alpha = groupForegroundAlphaActive
        } else {
            groupBackgroundPaint.alpha = groupBackgroundAlphaNotActive
            groupForegroundPaint.alpha = groupForegroundAlphaNotActive
        }
        canvas.drawRoundRect(
            left = groupX,
            top = yStart,
            right = groupX + groupWidth,
            bottom = yEnd,
            radius = groupRadius,
            paint = groupBackgroundPaint
        )
        val path = Path().also {
            it.reset()
            it.moveTo(groupX + groupRadius, yStart)
            it.appendArc(
                left = groupX,
                top = yStart,
                right = groupX + groupRadius * 2,
                bottom = yStart + groupRadius * 2,
                startAngle = 270f,
                sweepAngle = -90f,
                forceMoveTo = true
            )
            it.lineTo(groupX, yEnd - groupRadius)
            it.appendArc(
                left = groupX,
                top = yEnd - groupRadius * 2,
                right = groupX + groupRadius * 2,
                bottom = yEnd,
                startAngle = 180f,
                sweepAngle = -90f,
                forceMoveTo = true
            )
            it.lineTo(groupX + groupRadius, yStart)
            it.close()
        }
        canvas.drawPath(path, groupForegroundPaint)
        val textHeight = groupForegroundPaint.getTextHeight("0987654321")
        when (group.size) {
            1 -> {
                val event = group.first()
                canvas.drawText(event.payload, groupX + groupPaddingStart, yStart + textHeight + groupPaddingTop, groupForegroundPaint)
            }
            else -> {
                val map = group.groupBy { it.payload }
                val keys = map.keys.sortedByDescending {
                    map[it]?.size ?: 0
                }
//                val lineCountMax = groupLineCountMax
                val lineCountMax = if (groupLineCountMaxByGroupHeight) {
                    val count: Int = (groupHeight / (textHeight + groupTextLineSpace)).toInt() - 1 // todo
                    if (count <= 0) TODO()
                    val tmp = groupLineCountMax
                    if (tmp == null) count else kotlin.math.min(tmp, count)
                } else groupLineCountMax
                for (i in keys.indices) {
                    val payload = keys[i]
                    val list = map[payload]
                    if (list.isNullOrEmpty()) continue
                    val yText = yStart + textHeight * (i + 1) + groupTextLineSpace * i + groupPaddingTop
                    if (lineCountMax == null || i < lineCountMax) {
                        canvas.drawText(payload, groupX + groupPaddingStart, yText, groupForegroundPaint)
                        val rightText = list.size.toString()
                        val rightTextWidth = groupForegroundPaint.measureText(rightText)
                        canvas.drawText(rightText, groupX + groupWidth - rightTextWidth - groupPaddingEnd, yText, groupForegroundPaint)
                    } else {
                        val count = keys.subList(i, keys.size).sumBy {
                            map[it]?.size ?: 0
                        }
                        if (count < 1) break
                        canvas.drawText("...", groupX + groupPaddingStart, yText, groupForegroundPaint)
                        val rightText = "+$count"
                        val rightTextWidth = groupForegroundPaint.measureText(rightText)
                        canvas.drawText(rightText, groupX + groupWidth - rightTextWidth - groupPaddingEnd, yText, groupForegroundPaint)
                        break
                    }
                }
            }
        }
    }
    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        val timeRange = timeRange
        if (timeRange.isEmpty()) return
        val timeColumn = (timeRange step timeStepMinutes).toList()
        val timeTextHeight = timePaint.getTextHeight("0987654321:")
        val startingPointY = yOffset + paddingTop
        for (i in timeColumn.indices) {
            val time = timeColumn[i]
            val hourOfDay: Int = time / 60
            val value = String.format("%02d:%02d", hourOfDay, time - hourOfDay * 60)
            val y = startingPointY + timeStepHeight * i
            canvas.drawText(value, timeTextMargin, y + timeTextHeight / 2, timePaint)
            canvas.drawLine(timeLineMarginStart, y, width - timeLineMarginEnd, y, timeLinePaint)
            if (i == timeColumn.size - 1) break
            if (timeLineCount < 1) continue
            val lineHeight = timeStepHeight / (timeLineCount + 1)
            for (j in 0 until timeLineCount) {
                val lineY = y + lineHeight * (j + 1)
                canvas.drawLine(timeLineMarginStart, lineY, width - timeLineMarginEnd, lineY, timeLinePaint)
            }
        }
        val current = yearMonthDayCurrent
        val today = DateUtil.calendar(
            firstDayOfWeek = firstDayOfWeek,
            timeZone = timeZone
        )
        val ladderSize = ladderSize
        val groupMatrix = groupMatrix
        check(ladderSize == groupMatrix.size)
        groupMatrix.forEachIndexed { row, groups ->
            for (group in groups) {
                if (group.isEmpty()) continue
                val isActive = if (current == null) true else {
                    when {
                        today[Calendar.YEAR] > current.year -> false
                        today[Calendar.YEAR] == current.year -> when {
                            today[Calendar.MONTH] > current.month -> false
                            today[Calendar.MONTH] == current.month -> when {
                                today[Calendar.DAY_OF_MONTH] > current.dayOfMonth -> false
                                today[Calendar.DAY_OF_MONTH] == current.dayOfMonth -> group.maxOfOrNull { it.end }!! > today[Calendar.HOUR_OF_DAY] * 60 + today[Calendar.MINUTE]
                                else -> true
                            }
                            else -> true
                        }
                        else -> true
                    }
                }
                onDrawGroup(canvas = canvas, ladderSize = ladderSize, isActive = isActive, group = group, row = row)
            }
        }
        val timeMark = timeMark
        if (timeMark != null) {
            val dY = timeStepHeight / timeStepMinutes
            val y = startingPointY + timeMark * dY - timeRange.start * dY
            val alpha = timeMarkPaint.alpha
            canvas.drawLine(timeLineMarginStart, y, width.toFloat(), y, timeMarkPaint)
            timeMarkPaint.alpha = 255
            canvas.drawCircle(timeLineMarginStart, y, timeMarkRadius, timeMarkPaint)
            timeMarkPaint.alpha = alpha
        }
    }

    private fun onTouchDown(y: Float): Boolean {
        startedTrackingYOffset = yOffset - y
        return true
    }
    private var isMoveStarted = false
    private fun onTouchMove(heightChildren: Float, y: Float): Boolean {
        if (!isMoveStarted) {
            if ((yOffset - startedTrackingYOffset - y).absoluteValue < 5.5) return false
            isMoveStarted = true
        }
        if (heightChildren < height) return false
        yOffset = startedTrackingYOffset + y
        invalidate()
        return true
    }
    private fun onTouchUp(heightChildren: Float, x: Float, y: Float): Boolean {
        if (heightChildren > height) {
            if (yOffset > 0f) {
                startAnimateYOffset(0f)
            } else if (height - heightChildren > yOffset) {
                startAnimateYOffset(height - heightChildren)
            }
        }
        if (isMoveStarted) {
            isMoveStarted = false
            return true
        }
        if (x > groupMarginStart && x < width - groupMarginEnd) {
            val ladderSize = ladderSize
            if (ladderSize > 0) {
                val groupWidthFull: Float = width - groupMarginStart - groupMarginEnd
                val groupWidth: Float = (groupWidthFull - groupMargin * (ladderSize - 1)) / ladderSize
                for (row in 0 until ladderSize) {
                    val groupX: Float = groupMarginStart + groupWidth * row + groupMargin * row
                    if (x in groupX..(groupX + groupWidth)) {
                        val dY = timeStepHeight / timeStepMinutes
                        val startingPointY = yOffset + paddingTop
                        for (group in groupMatrix[row]) {
                            if (group.isEmpty()) continue
                            val start = group.minOfOrNull { it.start }!!
                            val yStart = startingPointY + start * dY - timeRange.start * dY
                            val end = group.maxOfOrNull { it.end }!!
                            val yEnd = startingPointY + end * dY - timeRange.start * dY
                            if (y in yStart..yEnd) {
                                onGroupClick(start, end)
                                break
                            }
                        }
                        break
                    }
                }
            }
        }
        return true
    }
    private fun onTouchEventSingle(heightChildren: Float, event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> onTouchDown(y = event.y)
            MotionEvent.ACTION_MOVE -> onTouchMove(heightChildren = heightChildren, y = event.y)
            MotionEvent.ACTION_UP -> onTouchUp(heightChildren = heightChildren, x = event.x, y = event.y)
            else -> false
        }
    }
    private fun onTouchEventMulti(heightChildren: Float, event: MotionEvent): Boolean {
        return when(event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> onTouchDown(y = event.y)
            MotionEvent.ACTION_MOVE -> onTouchMove(heightChildren = heightChildren, y = event.y)
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
    private fun getHeightChildren(): Float {
        val dY = timeStepHeight / timeStepMinutes
        return timeRange.endInclusive * dY - timeRange.start * dY + paddingTop + paddingBottom
    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        val heightChildren = getHeightChildren()
        return when (event.pointerCount) {
            1 -> onTouchEventSingle(heightChildren = heightChildren, event = event)
            else -> onTouchEventMulti(heightChildren = heightChildren, event = event)
        }
    }
}
