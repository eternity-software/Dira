package com.diraapp.ui.activities.resizer

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.view.View
import android.view.animation.DecelerateInterpolator


class FluidContentResizer {

    private var heightAnimator: ValueAnimator = ObjectAnimator()

    fun listen(activity: Activity) {
        val viewHolder = ActivityViewHolder.createFrom(activity)

        KeyboardVisibilityDetector.listen(viewHolder) {
            animateHeight(viewHolder, it)
        }
        viewHolder.onDetach {
            heightAnimator.cancel()
            heightAnimator.removeAllUpdateListeners()
        }
    }

    private fun animateHeight(viewHolder: ActivityViewHolder, event: KeyboardVisibilityChanged) {
        val contentView = viewHolder.contentView
        //contentView.setHeight(event.contentHeightBeforeResize)
        val startHeight = contentView.height
        heightAnimator.cancel()


        // Warning: animating height might not be very performant. Try turning on
        // "Profile GPI rendering" in developer options and check if the bars stay
        // under 16ms in your app. Using Transitions API would be more efficient, but
        // for some reason it skips the first animation and I cannot figure out why.
        heightAnimator = ObjectAnimator.ofInt(startHeight, event.contentHeight).apply {
            interpolator = DecelerateInterpolator(2f)
            duration = 250
        }
        heightAnimator.addUpdateListener { contentView.setHeight(it.animatedValue as Int) }
        heightAnimator.start()
    }

    private fun View.setHeight(height: Int) {
        val params = layoutParams
        params.height = height
        layoutParams = params
    }
}