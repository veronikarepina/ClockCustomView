package ru.veronikarepina.clockapp.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import ru.veronikarepina.clockapp.R
import java.util.*
import kotlin.properties.Delegates

class ClockView(
    context: Context,
    attributeSet: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
): View(context, attributeSet, defStyleAttr, defStyleRes) {

    private var clockStrokeColor by Delegates.notNull<Int>()
    private var pointsColor by Delegates.notNull<Int>()
    private var numbsColor by Delegates.notNull<Int>()
    private var hourHandColor by Delegates.notNull<Int>()
    private var minuteHandColor by Delegates.notNull<Int>()
    private var secondHandColor by Delegates.notNull<Int>()
    private var backgroundClockColor by Delegates.notNull<Int>()

    private var iconSrc by Delegates.notNull<Int>()
    private var iconWidth by Delegates.notNull<Float>()
    private var iconHeight by Delegates.notNull<Float>()

    private var clockRadius by Delegates.notNull<Float>()
    private var widthClockStroke by Delegates.notNull<Float>()
    private var widthSmallPoints by Delegates.notNull<Float>()
    private var widthBigPoints by Delegates.notNull<Float>()
    private var widthHourHand by Delegates.notNull<Float>()
    private var widthMinuteHand by Delegates.notNull<Float>()
    private var widthSecondHand by Delegates.notNull<Float>()

    private var sizeNumbs by Delegates.notNull<Float>()

    private var hourHandAngle by Delegates.notNull<Float>()
    private var minuteHandAngle by Delegates.notNull<Float>()
    private var secondHandAngle by Delegates.notNull<Float>()

    private lateinit var clockStrokePaint: Paint
    private lateinit var pointsPaint: Paint
    private lateinit var numbsPaint: Paint
    private lateinit var handsPaint: Paint
    private lateinit var handsShadowPaint: Paint
    private lateinit var icon: Paint
    private lateinit var drawable: Drawable
    private lateinit var bitmapSource: Bitmap
    private lateinit var bitmap: Bitmap
    private val calendar: Calendar = Calendar.getInstance()

    private val timer: Timer = Timer()
    private var taskSeconds = object : TimerTask(){
        override fun run() {
            if(secondHandAngle == 360F)
                secondHandAngle = 0F
            secondHandAngle += 6F
            postInvalidate()
        }
    }
    private var taskMinutesAndHours = object : TimerTask(){
        override fun run() {
            if(hourHandAngle == 360F)
                hourHandAngle = 0F
            if(minuteHandAngle == 360F)
                minuteHandAngle = 0F
            minuteHandAngle += 6F
            hourHandAngle += 0.1F
            postInvalidate()
        }
    }

    private var centerX = 0
    private var centerY = 0

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, R.style.DefaultClockStyle)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.clockStyle)
    constructor(context: Context) : this(context, null)

    init {
        if(attributeSet != null){
            initAttributes(attributeSet, defStyleAttr, defStyleRes)
        }else{
            initDefault()
        }
        initPaint()
        val hoursValue = calendar.get(Calendar.HOUR)
        val minutesValue = calendar.get(Calendar.MINUTE)
        val secondsValue = calendar.get(Calendar.SECOND)
        setTime(hoursValue, minutesValue, secondsValue)
        start()
    }

    private fun initAttributes(attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int){
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ClockView, defStyleAttr, defStyleRes)
        val sizeUtil = SizeUtils()
        clockStrokeColor = typedArray.getColor(R.styleable.ClockView_clockStrokeColor, CIRCLE_DEFAULT_COLOR)
        pointsColor = typedArray.getColor(R.styleable.ClockView_pointsColor, POINTS_DEFAULT_COLOR)
        numbsColor = typedArray.getColor(R.styleable.ClockView_numbsColor, NUMBS_DEFAULT_COLOR)
        hourHandColor = typedArray.getColor(R.styleable.ClockView_hourHandColor, HOURS_DEFAULT_COLOR)
        minuteHandColor = typedArray.getColor(R.styleable.ClockView_minuteHandColor, MINUTES_DEFAULT_COLOR)
        secondHandColor = typedArray.getColor(R.styleable.ClockView_secondHandColor, SECONDS_DEFAULT_COLOR)
        backgroundClockColor = typedArray.getColor(R.styleable.ClockView_backgroundClockColor, BACKGROUND_DEFAULT_COLOR)

        iconSrc = typedArray.getResourceId(R.styleable.ClockView_iconSrc, ICON_DEFAULT_SRC)
        iconWidth = typedArray.getDimension(R.styleable.ClockView_iconWidth, sizeUtil.dpToPx(context, ICON_DEFAULT_WIDTH))
        iconHeight = typedArray.getDimension(R.styleable.ClockView_iconHeight, sizeUtil.dpToPx(context, ICON_DEFAULT_HEIGHT))

        clockRadius = typedArray.getDimension(R.styleable.ClockView_clockRadius, sizeUtil.dpToPx(context, CLOCK_DEFAULT_RADIUS))
        widthClockStroke = typedArray.getDimension(R.styleable.ClockView_widthClockStroke, sizeUtil.dpToPx(context, STROKE_DEFAULT_WIDTH))
        widthSmallPoints = typedArray.getDimension(R.styleable.ClockView_widthSmallPoints, sizeUtil.dpToPx(context, WIDTH_DEFAULT_SMALL_POINTS))
        widthBigPoints = typedArray.getDimension(R.styleable.ClockView_widthBigPoints, sizeUtil.dpToPx(context, WIDTH_DEFAULT_BIG_POINTS))
        widthHourHand = typedArray.getDimension(R.styleable.ClockView_widthHourHand, sizeUtil.dpToPx(context, WIDTH_DEFAULT_HOUR_HAND))
        widthMinuteHand = typedArray.getDimension(R.styleable.ClockView_widthMinuteHand, sizeUtil.dpToPx(context, WIDTH_DEFAULT_MINUTE_HAND))
        widthSecondHand = typedArray.getDimension(R.styleable.ClockView_widthSecondHand, sizeUtil.dpToPx(context, WIDTH_DEFAULT_SECOND_HAND))
        sizeNumbs = typedArray.getDimension(R.styleable.ClockView_sizeNumbs, DEFAULT_SIZE_NUMBS)

        typedArray.recycle()
    }

    private fun initDefault(){
        clockStrokeColor = CIRCLE_DEFAULT_COLOR
        pointsColor = POINTS_DEFAULT_COLOR
        numbsColor = NUMBS_DEFAULT_COLOR
        hourHandColor = HOURS_DEFAULT_COLOR
        minuteHandColor = MINUTES_DEFAULT_COLOR
        secondHandColor = SECONDS_DEFAULT_COLOR
        backgroundClockColor = BACKGROUND_DEFAULT_COLOR
        iconSrc = ICON_DEFAULT_SRC
        iconWidth = ICON_DEFAULT_WIDTH
        iconHeight = ICON_DEFAULT_HEIGHT
        clockRadius = CLOCK_DEFAULT_RADIUS
        widthClockStroke = STROKE_DEFAULT_WIDTH
        widthSmallPoints = WIDTH_DEFAULT_SMALL_POINTS
        widthBigPoints = WIDTH_DEFAULT_BIG_POINTS
        widthHourHand = WIDTH_DEFAULT_HOUR_HAND
        widthMinuteHand = WIDTH_DEFAULT_MINUTE_HAND
        widthSecondHand = WIDTH_DEFAULT_SECOND_HAND
        sizeNumbs = DEFAULT_SIZE_NUMBS
    }

    private fun initPaint(){
        clockStrokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }

        pointsPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        numbsPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        icon = Paint().apply {
            isAntiAlias = true
        }

        handsPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeCap = Paint.Cap.ROUND
        }
        handsShadowPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeCap = Paint.Cap.ROUND
        }

        drawable = ResourcesCompat.getDrawable(resources, iconSrc, null)!!
        bitmapSource = drawable.toBitmap(iconWidth.toInt(), iconHeight.toInt(), null)
        bitmap = Bitmap.createBitmap(bitmapSource, 0, 0, bitmapSource.width, bitmapSource.height)
    }

    companion object{
        const val CIRCLE_DEFAULT_COLOR = Color.BLACK
        const val POINTS_DEFAULT_COLOR = Color.BLACK
        const val NUMBS_DEFAULT_COLOR = Color.BLACK
        const val HOURS_DEFAULT_COLOR = Color.BLACK
        const val MINUTES_DEFAULT_COLOR = Color.BLACK
        const val SECONDS_DEFAULT_COLOR = Color.BLACK
        const val BACKGROUND_DEFAULT_COLOR = Color.BLUE
        const val ICON_DEFAULT_SRC = -1
        const val ICON_DEFAULT_WIDTH = 80F
        const val ICON_DEFAULT_HEIGHT = 60F
        const val CLOCK_DEFAULT_RADIUS = 150F
        const val STROKE_DEFAULT_WIDTH = 12F
        const val WIDTH_DEFAULT_SMALL_POINTS = 3F
        const val WIDTH_DEFAULT_BIG_POINTS = 5F
        const val WIDTH_DEFAULT_HOUR_HAND = 15F
        const val WIDTH_DEFAULT_MINUTE_HAND = 8F
        const val WIDTH_DEFAULT_SECOND_HAND = 4F
        const val DEFAULT_SIZE_NUMBS = 20F
    }

    private fun start(){
        val start: Long = (60000 - calendar.get(Calendar.SECOND) * 1000 - 1000).toLong()
        timer.schedule(taskSeconds, 0, 1000)
        timer.schedule(taskMinutesAndHours, start, 60000)
    }

    private fun setTime(h: Int, m: Int, s: Int){
        hourHandAngle = if(h >= 12)
            (h + m / 60F - 12) * 30F - 180
        else
            (h + m / 60F) * 30F - 180
        minuteHandAngle = m * 6F - 180
        secondHandAngle = s * 6F - 180
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2
        centerY = h / 2
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desireWidth = clockRadius.toInt() * 2 + widthClockStroke.toInt() * 2
        val desireHeight = clockRadius.toInt() * 2 + widthClockStroke.toInt() * 2
        val resolvedWidth = resolveSize(desireWidth, widthMeasureSpec)
        val resolvedHeight = resolveSize(desireHeight, heightMeasureSpec)

        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.translate(centerX.toFloat(), centerY.toFloat())
        drawCircle(canvas)
        drawNumbs(canvas)
        drawHands(canvas)
    }

    private fun drawCircle(canvas: Canvas?){
        canvas?.save()
        clockStrokePaint.style = Paint.Style.FILL
        clockStrokePaint.color = backgroundClockColor
        canvas?.drawCircle(0F, 0F, clockRadius, clockStrokePaint)
        clockStrokePaint.style = Paint.Style.STROKE
        clockStrokePaint.strokeWidth = widthClockStroke
        clockStrokePaint.color = clockStrokeColor
        canvas?.drawCircle(0F, 0F, clockRadius, clockStrokePaint)
        for (i in 0 until 60){
            pointsPaint.color = pointsColor
            if (i % 5 == 0){
                pointsPaint.strokeWidth = widthBigPoints
            }
            else {
                pointsPaint.strokeWidth = widthSmallPoints
            }
            canvas?.drawPoint(0F, -clockRadius + widthClockStroke, pointsPaint)
            canvas?.rotate(6F)
        }
        canvas?.restore()
        canvas?.drawBitmap(bitmap, (-bitmap.width/2).toFloat(), (-bitmap.height/2).toFloat(), icon)
    }

    private fun drawNumbs(canvas: Canvas?){
        canvas?.save()
        numbsPaint.textSize = sizeNumbs
        numbsPaint.color = numbsColor
        canvas?.rotate(30F)
        for(i in 1..12){
            canvas?.save()
            val textBound = Rect()
            canvas?.translate(0F, (-clockRadius + widthBigPoints + widthSmallPoints + widthClockStroke * 2))
            val text: String = i.toString()
            numbsPaint.getTextBounds(text, 0, text.length, textBound)
            canvas?.rotate(-i*30F)
            canvas?.drawText(text, (-textBound.width()/2).toFloat(), (textBound.height()/2).toFloat(), numbsPaint)
            canvas?.restore()
            canvas?.rotate(30F)
        }
        canvas?.restore()
    }

    private fun drawHands(canvas: Canvas?){
        canvas?.save()
        handsShadowPaint.color = Color.BLACK
        handsShadowPaint.strokeWidth = widthHourHand
        handsShadowPaint.alpha = 60
        canvas?.rotate(hourHandAngle, 8f, 8f)
        canvas?.drawLine(0f, -45f, 0f, (clockRadius*0.4).toFloat(), handsShadowPaint)
        canvas?.restore()

        canvas?.save()
        handsShadowPaint.color = Color.BLACK
        handsShadowPaint.strokeWidth = widthMinuteHand
        handsShadowPaint.alpha = 60
        canvas?.rotate(minuteHandAngle, 8f, 8f)
        canvas?.drawLine(0f, -45f, 0f, (clockRadius*0.55).toFloat(), handsShadowPaint)
        canvas?.restore()

        canvas?.save()
        handsShadowPaint.color = Color.BLACK
        handsShadowPaint.strokeWidth = widthSecondHand
        handsShadowPaint.alpha = 60
        canvas?.rotate(secondHandAngle, 8f, 8f)
        canvas?.drawLine(0f, -45f, 0f, (clockRadius*0.7).toFloat(), handsShadowPaint)
        canvas?.restore()

        canvas?.save()
        handsPaint.color = hourHandColor
        handsPaint.strokeWidth = widthHourHand
        canvas?.rotate(hourHandAngle, 0F, 0F)
        canvas?.drawLine(0F, -40F, 0F, (clockRadius*0.45).toFloat(), handsPaint)
        canvas?.restore()

        canvas?.save()
        handsPaint.color = minuteHandColor
        handsPaint.strokeWidth = widthMinuteHand
        canvas?.rotate(minuteHandAngle, 0F, 0F)
        canvas?.drawLine(0F, -40F, 0F, (clockRadius*0.6).toFloat(), handsPaint)
        canvas?.restore()

        canvas?.save()
        handsPaint.color = secondHandColor
        handsPaint.strokeWidth = widthSecondHand
        canvas?.rotate(secondHandAngle, 0F, 0F)
        canvas?.drawLine(0F, -40F, 0F, (clockRadius*0.75).toFloat(), handsPaint)
        canvas?.restore()
    }

    private class SizeUtils{
        fun dpToPx(context: Context, dp: Float): Float{
            val density: Float = context.resources.displayMetrics.density
            return dp * density + 0.5F
        }
        fun pxToDp(context: Context, px: Float): Float{
            val density: Float = context.resources.displayMetrics.density
            return px / density + 0.5F
        }
    }
}