package com.example.analogclockexample

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.util.*
import kotlin.math.cos
import kotlin.math.sin


class AnalogClock : View {

    enum class DialType {
        NUMBERS,
        ROMAN_NUMBERS,
        CIRCLE,
        NONE
    }

    enum class HandType {
        SECOND,
        MINUTE,
        HOUR
    }

    private val romanNumerals: Map<Int, String> = mapOf(
        1 to "I",
        2 to "II",
        3 to "III",
        4 to "IV",
        5 to "V",
        6 to "VI",
        7 to "VII",
        8 to "VIII",
        9 to "IX",
        10 to "X",
        11 to "XI",
        12 to "XII",
    )

    private var dialType: DialType = DialType.ROMAN_NUMBERS
    private var dialRadius = 300f
    private var dialElementSize = 50f

    private var centerRadius = 20f

    private var handMinutesSize = 0.7f
    private var handSecondsSize = 0.9f
    private var handOrbitSize = 0.5f
    private var borderOffset = 0f
    private var delayUpdate = 10L

    private var centerX: Float = (width / 2).toFloat()
    private var centerY: Float = (height / 2).toFloat()

    private var paintHandsMap = mapOf(
        HandType.HOUR to Paint(),
        HandType.SECOND to Paint(),
        HandType.MINUTE to Paint(),
    )
    private var paintDial = Paint()
    private var paintBorder = Paint()
    private var paintBackground = Paint()
    private var paintCenter = Paint()

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val attributes = context.obtainStyledAttributes(
            attrs, R.styleable.AnalogClock, defStyle, 0
        )
        dialType = DialType.values()[attributes.getInt(R.styleable.AnalogClock_dialType, 0)]
        dialElementSize = attributes.getFloat(R.styleable.AnalogClock_dialElementSize, 80f)
        dialRadius = attributes.getFloat(R.styleable.AnalogClock_dialRadiusOrbit, 300f)

        centerRadius = attributes.getFloat(R.styleable.AnalogClock_centerRadiusOrbit, 20f)

        borderOffset = attributes.getFloat(R.styleable.AnalogClock_borderOffset, 50f)

        handSecondsSize = attributes.getFloat(R.styleable.AnalogClock_secondHandSize, 0.9f)
        handMinutesSize = attributes.getFloat(R.styleable.AnalogClock_minuteHandSize, 0.7f)
        handOrbitSize = attributes.getFloat(R.styleable.AnalogClock_hourHandSize, 0.5f)

        paintBorder.apply {
            color = attributes.getColor(R.styleable.AnalogClock_borderColor, Color.BLACK)
            strokeWidth = attributes.getFloat(R.styleable.AnalogClock_borderThickness, 10f)
            style = Paint.Style.STROKE
        }
        paintHandsMap[HandType.SECOND]!!.apply {
            strokeWidth = attributes.getFloat(R.styleable.AnalogClock_secondHandThickness, 10f)
            color = attributes.getColor(R.styleable.AnalogClock_secondHandColor, Color.BLACK)
        }
        paintHandsMap[HandType.MINUTE]!!.apply {
            strokeWidth = attributes.getFloat(R.styleable.AnalogClock_minuteHandThickness, 10f)
            color = attributes.getColor(R.styleable.AnalogClock_minuteHandColor, Color.BLACK)
        }
        paintHandsMap[HandType.HOUR]!!.apply {
            strokeWidth = attributes.getFloat(R.styleable.AnalogClock_hourHandThickness, 10f)
            color = attributes.getColor(R.styleable.AnalogClock_hourHandColor, Color.BLACK)
        }

        paintCenter.apply {
            color = attributes.getColor(R.styleable.AnalogClock_centerColor, Color.BLACK)
        }

        paintDial.apply {
            textSize = dialElementSize
            textAlign = Paint.Align.CENTER
            typeface = getFont(attributes, R.styleable.AnalogClock_dialTextFont)
            color = attributes.getColor(R.styleable.AnalogClock_dialColor, Color.BLACK)
        }

        paintBackground.apply {
            color = attributes.getColor(R.styleable.AnalogClock_backgroundColor, Color.BLACK)
            style = Paint.Style.FILL
        }

        attributes.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        calculateCenter()
        drawBackground(canvas)
        drawBorder(canvas)
        drawDial(canvas)
        drawHands(canvas)
        postInvalidateDelayed(delayUpdate)
    }

    private fun getTime(): Triple<Float, Float, Float> {
        val date = Calendar.getInstance()
        val seconds = date.get(Calendar.SECOND).toFloat() + date.get(Calendar.MILLISECOND) / 1000f
        val minutes = date.get(Calendar.MINUTE).toFloat() + seconds / 60f
        val hours = date.get(Calendar.HOUR).toFloat() + minutes / 60f
        return Triple(seconds, minutes, hours)
    }

    private fun getFont(attributes: TypedArray, index: Int): Typeface? {
        val fontAttr = attributes.getString(index)
        var font = Typeface.DEFAULT
        try {
            font = Typeface.createFromAsset(context.assets, fontAttr)
        } catch (e: Exception) {
            Log.e("ASSET ERROR", "init: ${e.message}")
        }
        return font
    }

    private fun getAngle(parts: Int, currentPart: Float): Double {
        return 2 * Math.PI / parts * (currentPart - parts / 4)
    }

    private fun calculateCenter() {
        centerX = (width / 2).toFloat()
        centerY = (height / 2).toFloat()
    }

    private fun drawDial(canvas: Canvas) {
        for (i in 1..12) {
            val angle = getAngle(12, i.toFloat())
            val x = (centerX + cos(angle) * (dialRadius)).toFloat()
            val y = (centerY + sin(angle) * (dialRadius)).toFloat()
            val textOffsetY = paintDial.textSize / 2 - paintDial.fontMetrics.leading
            when (dialType) {
                DialType.CIRCLE -> canvas.drawCircle(x, y, dialElementSize, paintDial)
                DialType.ROMAN_NUMBERS -> canvas.drawText(romanNumerals[i]!!, x, y + textOffsetY, paintDial)
                DialType.NUMBERS -> canvas.drawText(i.toString(), x, y + textOffsetY, paintDial)
                DialType.NONE -> {}
            }
        }
    }

    private fun drawHand(canvas: Canvas, angle: Double, handLength: Float, type: HandType) {
        val x = (centerX + cos(angle) * handLength).toFloat()
        val y = (centerY + sin(angle) * handLength).toFloat()
        canvas.drawLine(centerX, centerY, x, y, paintHandsMap[type]!!)
    }

    private fun drawHands(canvas: Canvas) {
        val (seconds, minutes, hours) = getTime()
        val hourAngle = getAngle(12, hours)
        val minuteAngle = getAngle(60, minutes)
        val secondAngle = getAngle(60, seconds)
        drawHand(canvas, hourAngle, dialRadius * handOrbitSize, HandType.HOUR)
        drawHand(canvas, minuteAngle, dialRadius * handMinutesSize, HandType.MINUTE)
        drawHand(canvas, secondAngle, dialRadius * handSecondsSize, HandType.SECOND)
        canvas.drawCircle(centerX, centerY, centerRadius, paintCenter)
    }

    private fun drawBorder(canvas: Canvas) {
        canvas.drawCircle(
            centerX,
            centerY,
            dialRadius + borderOffset + paintBorder.strokeWidth / 2,
            paintBorder
        )
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, dialRadius + borderOffset, paintBackground)
    }
}