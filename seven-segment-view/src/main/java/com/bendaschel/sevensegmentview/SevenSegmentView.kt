package com.bendaschel.sevensegmentview

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import java.util.*

class SevenSegmentView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    var onColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var offColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var currentValue = DIGITS.ALL_OFF.ordinal
        set(currentValue) {
            assert(currentValue in 0..9)
            field = currentValue
            invalidate()
        }

    private var mActiveSegments: BooleanArray = DEFAULT_STATE.activeSegments
        get() = if (currentValue in 0..9) {
            DIGITS.values()[currentValue].activeSegments
        } else {
            DEFAULT_STATE.activeSegments
        }

    private val mCurrentTransformation: Matrix
    private val mTemporaryPath: Path
    private val mPaint: Paint

    private enum class SEGMENTS private constructor(rotate: Int, translateX: Int, translateY: Int) {
        TOP(0, 0, 10),
        TOP_RIGHT(90, 5, 5),
        BOTTOM_RIGHT(90, 5, -5),
        BOTTOM(0, 0, -10),
        BOTTOM_LEFT(90, -5, -5),
        TOP_LEFT(90, -5, 5),
        CENTER(0, 0, 0);

        internal val path: Path

        init {
            val matrix = Matrix()
            // Yes, this must be -Y because the graphics coord system is upside-down
            // from a normal cartesian plane
            matrix.setTranslate(translateX.toFloat(), (-translateY).toFloat())
            matrix.preRotate(rotate.toFloat())
            path = Path()
            BASE_PATH.transform(matrix, path)
        }
    }

    private enum class DIGITS constructor(internal vararg var activeSegments: Boolean) {
        ZERO(ON, ON, ON, ON, ON, ON, OFF),
        ONE(OFF, ON, ON, OFF, OFF, OFF, OFF),
        TWO(ON, ON, OFF, ON, ON, OFF, ON),
        THREE(ON, ON, ON, ON, OFF, OFF, ON),
        FOUR(OFF, ON, ON, OFF, OFF, ON, ON),
        FIVE(ON, OFF, ON, ON, OFF, ON, ON),
        SIX(ON, OFF, ON, ON, ON, ON, ON),
        SEVEN(ON, ON, ON, OFF, OFF, OFF, OFF),
        EIGHT(ON, ON, ON, ON, ON, ON, ON),
        NINE(ON, ON, ON, OFF, OFF, ON, ON),
        ALL_OFF(OFF, OFF, OFF, OFF, OFF, OFF, OFF);
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SevenSegmentView, 0, 0)
        offColor = a.getColor(R.styleable.SevenSegmentView_offColor, Color.argb(50, Color.red(Color.RED), 0, 0))
        onColor = a.getColor(R.styleable.SevenSegmentView_onColor, Color.RED)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCurrentTransformation = Matrix()
        mTemporaryPath = Path()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // https://stackoverflow.com/questions/12266899/onmeasure-custom-view-explanation
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val scaleFactor = calculateScaleFactor(widthSize.toFloat(), heightSize.toFloat(),
                SEGMENT_BOUNDS.width(), SEGMENT_BOUNDS.height())
        val desiredWidth = (SEGMENT_BOUNDS.width() * scaleFactor).toInt()
        val desiredHeight = (SEGMENT_BOUNDS.height() * scaleFactor).toInt()
        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        mCurrentTransformation.reset()
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height
        mCurrentTransformation.setTranslate((canvasWidth / 2).toFloat(), (canvasHeight / 2).toFloat())
        // Scale properly to the aspect ratio of the canvas
        val drawHeight = canvasHeight - paddingBottom - paddingTop
        val drawWidth = canvasWidth - paddingLeft - paddingRight
        val scaleFactor = calculateScaleFactor(drawWidth.toFloat(), drawHeight.toFloat(),
                SEGMENT_BOUNDS.width(), SEGMENT_BOUNDS.height())
        mCurrentTransformation.preScale(scaleFactor, scaleFactor)

        for (segment in SEGMENTS.values()) {
            segment.path.transform(mCurrentTransformation, mTemporaryPath)
            val position = segment.ordinal
            mPaint.color = if (mActiveSegments!![position]) onColor else offColor
            canvas.drawPath(mTemporaryPath, mPaint)
            mTemporaryPath.reset()
        }
    }

    private fun calculateScaleFactor(containerWidth: Float, containerHeight: Float,
                                     childWidth: Float, childHeight: Float): Float {
        val heightRatio = containerHeight / childHeight
        val widthRatio = containerWidth / childWidth
        return Math.min(widthRatio, heightRatio)
    }

    override fun onSaveInstanceState(): Parcelable? {
        // https://stackoverflow.com/questions/3542333/how-to-prevent-custom-views-from-losing-state-across-screen-orientation-changes
        // Much less of a PITA than implementing a custom parcelable to hold one value;
        val bundle = Bundle()
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
        bundle.putInt(KEY_SAVE_STATE, currentValue)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState: Bundle? = null
        if (state is Bundle) {
            currentValue = state.getInt(KEY_SAVE_STATE, DEFAULT_STATE.ordinal)
            superState = state.getParcelable(KEY_SUPER_STATE)
        }
        super.onRestoreInstanceState(superState)
    }

    companion object {

        private val BASE_PATH = PathUtils.makePathFromPoints(Arrays.asList(
                Point(4, 1),
                Point(5, 0),
                Point(4, -1),
                Point(-4, -1),
                Point(-5, 0),
                Point(-4, 1)
        ))
        private val KEY_SAVE_STATE = "saved_current_value"
        private val DEFAULT_STATE = DIGITS.ALL_OFF
        val KEY_SUPER_STATE = "superState"

        private val SEGMENT_BOUNDS = calculateAllSegmentBounds()
        private val ON = true
        private val OFF = false

        private fun calculateAllSegmentBounds(): RectF {
            val outerBounds = RectF()
            val segmentBounds = RectF()
            SEGMENTS.TOP.path.computeBounds(segmentBounds, true)
            for (segment in SEGMENTS.values()) {
                segment.path.computeBounds(segmentBounds, true)
                outerBounds.union(segmentBounds)
            }
            return outerBounds
        }
    }
}
