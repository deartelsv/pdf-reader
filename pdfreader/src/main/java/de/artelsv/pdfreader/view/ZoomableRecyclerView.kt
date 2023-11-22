package de.artelsv.pdfreader.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.animation.DecelerateInterpolator
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import java.lang.Float.isNaN

@SuppressLint("NewApi")
class ZoomableRecyclerView(private val context: Context) : RecyclerView(context, null) {
    // слушатели для зума и нажатий
    private var mScaleDetector = ScaleGestureDetector(context, ScaleListener())
    private var mGestureDetector = GestureDetectorCompat(context, GestureListener())

    // конфигурация вьюхи
    var mMaxScaleFactor = DEFAULT_MAX_SCALE_FACTOR
    var mMinScaleFactor = DEFAULT_MIN_SCALE_FACTOR
    var mDefaultScaleFactor = DEFAULT_SCALE_FACTOR
    var mScaleDuration = DEFAULT_SCALE_DURATION

    // параметры необходимые для отрисовки
    var mViewWidth = 0f // ширина вьюхи
    var mViewHeight = 0f // высота вьюхи
    var mTranX = 0f // смещение по оси x
    var mTranY = 0f // смещение по оси y
    var mScaleFactor = mDefaultScaleFactor // коэффициент масштабирования

    // touch param
    private var mActivePointerId = MotionEvent.INVALID_POINTER_ID // активный идентификатор касания
    private var mLastTouchX = 0f // Место последнего касания X
    private var mLastTouchY = 0f // Место последнего касания Y

    // control param
    var isScaling = false // выполняется ли масштабирование
    var isZoomEnabled = false // включено ли масштабирование

    // zoom param
    private var mScaleAnimator: ValueAnimator? = null // аниматор для масштабирования
    var mScaleCenterX = 0f // центр масштабирования по X
    var mScaleCenterY = 0f // центр масштабирования по Y
    var mMaxTranX = 0f // Максимальное смещение X при текущем коэффициенте масштабирования
    var mMaxTranY = 0f // Максимальное смещение Н при текущем коэффициенте масштабирования

    private val linearLayoutManager by lazy { ExtraSpaceLinearLayoutManager(context) }

    init {
        layoutManager = linearLayoutManager.apply { orientation = VERTICAL }
        setBackgroundColor(Color.parseColor("#F8FAFC"))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mViewWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!isZoomEnabled) {
            return super.onTouchEvent(ev)
        }
        var retVal = mScaleDetector.onTouchEvent(ev)
        retVal = mGestureDetector.onTouchEvent(ev) || retVal
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val pointerIndex = ev.actionIndex
                val x = ev.getX(pointerIndex)
                val y = ev.getY(pointerIndex)
                // Запоминааем откуда мы начали перетаскивать
                mLastTouchX = x
                mLastTouchY = y
                // Сохраняем идентификатор этого указателя (для перетаскивания)
                mActivePointerId = ev.getPointerId(0)
            }

            MotionEvent.ACTION_MOVE -> {
                try {
                    // Ищем индекс активного указателя
                    val pointerIndex = ev.findPointerIndex(mActivePointerId)
                    val x = ev.getX(pointerIndex)
                    val y = ev.getY(pointerIndex)
                    if (!isScaling && mScaleFactor > 1) { // 缩放时不做处理
                        // Считаем разницу (пройденный путь)
                        val dx = x - mLastTouchX
                        val dy = y - mLastTouchY
                        setTranslateXY(mTranX + dx, mTranY + dy)
                        correctTranslateXY()
                    }
                    invalidate()
                    // Сохраняем позицию касания для последующего использования
                    mLastTouchX = x
                    mLastTouchY = y
                } catch (e: Exception) {
                    val x = ev.x
                    val y = ev.y
                    if (!isScaling && mScaleFactor > 1 && mLastTouchX != INVALID_TOUCH_POSITION) { // 缩放时不做处理
                        // Считаем разницу (пройденный путь)
                        val dx = x - mLastTouchX
                        val dy = y - mLastTouchY
                        setTranslateXY(mTranX + dx, mTranY + dy)
                        correctTranslateXY()
                    }
                    invalidate()
                    // Сохраняем позицию касания для последующего использования
                    mLastTouchX = x
                    mLastTouchY = y
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID
                mLastTouchX = INVALID_TOUCH_POSITION
                mLastTouchY = INVALID_TOUCH_POSITION
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = ev.actionIndex
                val pointerId = ev.getPointerId(pointerIndex)
                if (pointerId == mActivePointerId) {
                    // Это был наш активный указатель.
                    // Выбираем новый активный указатель и настраиваем его.
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mLastTouchX = ev.getX(newPointerIndex)
                    mLastTouchY = ev.getY(newPointerIndex)
                    mActivePointerId = ev.getPointerId(newPointerIndex)
                }
            }
        }
        return super.onTouchEvent(ev) || retVal
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()
        canvas.translate(mTranX, mTranY)
        canvas.scale(mScaleFactor, mScaleFactor)

        super.dispatchDraw(canvas)
        canvas.restore()
    }

    private fun setTranslateXY(tranX: Float, tranY: Float) {
        mTranX = if (tranX.isNaN()) 0f else tranX
        mTranY = if (tranY.isNaN()) 0f else tranY
    }

    // Корректировка смещения с учетом границ, когда масштаб больше чем 1.
    private fun correctTranslateXY() {
        val correctXY = correctTranslateXY(mTranX, mTranY)
        mTranX = if (correctXY[0].isNaN()) 0f else correctXY[0]
        mTranY = if (correctXY[1].isNaN()) 0f else correctXY[1]
    }

    private fun correctTranslateXY(mX: Float, mY: Float): FloatArray {
        var x = mX
        var y = mY
        if (mScaleFactor <= 1) {
            return floatArrayOf(x, y)
        }
        if (x > 0.0f) {
            x = 0.0f
        } else if (x < mMaxTranX) {
            x = mMaxTranX
        }
        if (y > 0.0f) {
            y = 0.0f
        } else if (y < mMaxTranY) {
            y = mMaxTranY
        }
        return floatArrayOf(x, y)
    }

    private fun zoom(startVal: Float, endVal: Float) {
        if (mScaleAnimator == null) {
            newZoomAnimation()
        }
        if (mScaleAnimator?.isRunning != false) {
            return
        }
        mMaxTranX = mViewWidth - mViewWidth * endVal
        mMaxTranY = mViewHeight - mViewHeight * endVal
        val startTranX = mTranX
        val startTranY = mTranY
        var endTranX = mTranX - (endVal - startVal) * mScaleCenterX
        var endTranY = mTranY - (endVal - startVal) * mScaleCenterY
        val correct = correctTranslateXY(endTranX, endTranY)
        endTranX = correct[0]
        endTranY = correct[1]
        val scaleHolder = PropertyValuesHolder
            .ofFloat(PROPERTY_SCALE, startVal, endVal)
        val tranXHolder = PropertyValuesHolder
            .ofFloat(PROPERTY_TRAN_X, startTranX, endTranX)
        val tranYHolder = PropertyValuesHolder
            .ofFloat(PROPERTY_TRAN_Y, startTranY, endTranY)

        mScaleAnimator?.let { scaleAnimator ->
            scaleAnimator.setValues(scaleHolder, tranXHolder, tranYHolder)
            scaleAnimator.duration = mScaleDuration.toLong()
            scaleAnimator.start()
        }
    }

    private fun scaling(value: Boolean = true) {
        isScaling = value
    }

    private fun newZoomAnimation() {
        mScaleAnimator = ValueAnimator()

        mScaleAnimator?.let { scaleAnimator ->
            scaleAnimator.interpolator = DecelerateInterpolator()
            scaleAnimator.addUpdateListener { animation -> // update scaleFactor & tranX & tranY
                mScaleFactor =
                    animation.getAnimatedValue(PROPERTY_SCALE) as Float
                setTranslateXY(
                    animation.getAnimatedValue(PROPERTY_TRAN_X) as Float,
                    animation.getAnimatedValue(PROPERTY_TRAN_Y) as Float
                )
                invalidate()
            }

            // слушатель для обновления флага масштабирования
            scaleAnimator.addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) = scaling()
                    override fun onAnimationEnd(animation: Animator) = scaling(false)
                    override fun onAnimationCancel(animation: Animator) = scaling(false)
                }
            )
        }
    }

    // обработка события масштабирования
    private inner class ScaleListener : OnScaleGestureListener {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val mLastScale = mScaleFactor
            mScaleFactor *= detector.scaleFactor
            // изменить коэффициент масштабирования
            mScaleFactor = mMinScaleFactor.coerceAtLeast(mScaleFactor.coerceAtMost(mMaxScaleFactor))
            mMaxTranX = mViewWidth - mViewWidth * mScaleFactor
            mMaxTranY = mViewHeight - mViewHeight * mScaleFactor
            correctTranslateXY()
            mScaleCenterX = detector.focusX
            mScaleCenterY = detector.focusY
            val offsetX = mScaleCenterX * (mLastScale - mScaleFactor)
            val offsetY = mScaleCenterY * (mLastScale - mScaleFactor)
            setTranslateXY(mTranX + offsetX, mTranY + offsetY)
            isScaling = true
            invalidate()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            if (mScaleFactor <= mDefaultScaleFactor) {
                mScaleCenterX = -mTranX / (mScaleFactor - 1)
                mScaleCenterY = -mTranY / (mScaleFactor - 1)
                mScaleCenterX = if (isNaN(mScaleCenterX)) 0f else mScaleCenterX
                mScaleCenterY = if (isNaN(mScaleCenterY)) 0f else mScaleCenterY
                zoom(mScaleFactor, mDefaultScaleFactor)
            }
            isScaling = false
        }
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val startFactor = mScaleFactor
            val endFactor: Float
            if (mScaleFactor == mDefaultScaleFactor) {
                mScaleCenterX = e.x
                mScaleCenterY = e.y
                endFactor = mMaxScaleFactor
            } else {
                mScaleCenterX = if (mScaleFactor == 1f) e.x else -mTranX / (mScaleFactor - 1)
                mScaleCenterY = if (mScaleFactor == 1f) e.y else -mTranY / (mScaleFactor - 1)
                endFactor = mDefaultScaleFactor
            }
            zoom(startFactor, endFactor)
            return super.onDoubleTap(e)
        }
    }

    companion object {
        private const val DEFAULT_SCALE_DURATION = 300
        private const val DEFAULT_SCALE_FACTOR = 1f
        private const val DEFAULT_MAX_SCALE_FACTOR = 2.0f
        private const val DEFAULT_MIN_SCALE_FACTOR = 0.5f
        private const val PROPERTY_SCALE = "scale"
        private const val PROPERTY_TRAN_X = "tranX"
        private const val PROPERTY_TRAN_Y = "tranY"
        private const val INVALID_TOUCH_POSITION = -1f
    }
}