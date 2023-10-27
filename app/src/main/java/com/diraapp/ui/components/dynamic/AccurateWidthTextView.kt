package com.diraapp.ui.components.dynamic

import android.graphics.Canvas
import android.text.Layout
import android.text.Layout.Alignment.ALIGN_CENTER
import android.text.Layout.Alignment.ALIGN_NORMAL
import android.text.Layout.Alignment.ALIGN_OPPOSITE
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.ceil

/**
 * Created by Max Diland
 */

/**
 * Improved solution
 * https://stackoverflow.com/questions/7439748/why-is-wrap-content-in-multiple-line-textview-filling-parent
 * It is a hacky implementation and because of the hack please use it to display texts only!
 * Now it supports any textAlignment, RTL.
 * What is not supported (supported but unusable):
 * - compound drawables,
 * - background drawables
 */
class AccurateWidthTextView @JvmOverloads constructor(
        context: android.content.Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var extraPaddingRight: Int? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (layout == null || layout.lineCount < 2) return

        val maxLineWidth = ceil(getMaxLineWidth(layout)).toInt()
        val uselessPaddingWidth = layout.width - maxLineWidth

        val width = measuredWidth - uselessPaddingWidth
        val height = measuredHeight
        setMeasuredDimension(width, height)

    }

    private fun getMaxLineWidth(layout: Layout): Float {
        return (0 until layout.lineCount)
                .map { layout.getLineWidth(it) }
                .max()
                ?: 0.0f
    }


    override fun onDraw(canvas: Canvas) {
        if (layout == null || layout.lineCount < 2) return super.onDraw(canvas)

        val explicitLayoutAlignment = layout.getExplicitAlignment()
        if (explicitLayoutAlignment == ExplicitLayoutAlignment.MIXED) return super.onDraw(canvas)

        val layoutWidth = layout.width
        val maxLineWidth = ceil(getMaxLineWidth(layout)).toInt()

        if (layoutWidth == maxLineWidth) return super.onDraw(canvas)

        when (explicitLayoutAlignment) {
            ExplicitLayoutAlignment.RIGHT -> {
                drawTranslatedHorizontally(
                        canvas,
                        -1 * (layoutWidth - maxLineWidth)
                ) { super.onDraw(it) }
                return
            }

            ExplicitLayoutAlignment.CENTER -> {
                drawTranslatedHorizontally(
                        canvas,
                        -1 * (layoutWidth - maxLineWidth) / 2
                ) { super.onDraw(it) }
                return
            }

            else -> return super.onDraw(canvas)
        }
    }

    private fun drawTranslatedHorizontally(
            canvas: Canvas,
            xTranslation: Int,
            drawingAction: (Canvas) -> Unit
    ) {
        extraPaddingRight = xTranslation
        canvas.save()
        canvas.translate(xTranslation.toFloat(), 0f)
        drawingAction.invoke(canvas)
        extraPaddingRight = null
        canvas.restore()
    }

    /*
    This textView does not support compound drawables correctly so the function is used not on purpose.
    It affects clipRect's width which gets formed inside the onDraw() method.
    Negative - increases.
    Positive - shrinks
    So before onDraw you should set some value to the field extraPaddingRight
    to change clip rect bounds and set null right after onDraw
     */
    override fun getCompoundPaddingRight(): Int {
        return extraPaddingRight ?: super.getCompoundPaddingRight()
    }
}

/*
It does not matter whether the text is LTR or RLT at the end of the day it is either aligned left
or right or centered. Mixed means the layout has more than 1 paragraph and the paragraphs have
different alignments
 */
private enum class ExplicitLayoutAlignment {
    LEFT, CENTER, RIGHT, MIXED
}

private fun Layout.getExplicitAlignment(): ExplicitLayoutAlignment {
    if (lineCount == 0) return ExplicitLayoutAlignment.LEFT

    val explicitAlignments = (0 until this.lineCount)
            .mapNotNull { this.getLineExplicitAlignment(it) }
            .distinct()

    return if (explicitAlignments.size > 1) {
        ExplicitLayoutAlignment.MIXED
    } else {
        explicitAlignments.firstOrNull() ?: ExplicitLayoutAlignment.LEFT
    }
}

private fun Layout.getLineExplicitAlignment(line: Int): ExplicitLayoutAlignment? {
    if (line !in 0 until this.lineCount) return null

    val isDirectionLtr = getParagraphDirection(line) == Layout.DIR_LEFT_TO_RIGHT
    val alignment = getParagraphAlignment(line)

    return when {
        alignment.name == "ALIGN_RIGHT" -> ExplicitLayoutAlignment.RIGHT
        alignment.name == "ALIGN_LEFT" -> ExplicitLayoutAlignment.LEFT
        // LTR and RTL
        alignment == ALIGN_CENTER -> ExplicitLayoutAlignment.CENTER
        // LTR
        isDirectionLtr && alignment == ALIGN_NORMAL -> ExplicitLayoutAlignment.LEFT
        isDirectionLtr && alignment == ALIGN_OPPOSITE -> ExplicitLayoutAlignment.RIGHT
        // RTL
        alignment == ALIGN_NORMAL -> ExplicitLayoutAlignment.RIGHT
        else -> ExplicitLayoutAlignment.LEFT
    }
}