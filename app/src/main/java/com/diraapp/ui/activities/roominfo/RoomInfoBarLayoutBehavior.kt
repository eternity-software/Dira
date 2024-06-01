package com.diraapp.ui.activities.roominfo

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.diraapp.R
import com.diraapp.ui.components.FadingImageView
import com.diraapp.utils.Logger
import com.google.android.material.appbar.AppBarLayout
import com.masoudss.lib.utils.Utils
import kotlin.math.sqrt

class RoomInfoBarLayoutBehavior: AppBarLayout.Behavior {

    private var previousProgress: Float = 0.0F

    private lateinit var fadeImage: FadingImageView

    private lateinit var toolbar: Toolbar

    constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet)

    override fun onLayoutChild(parent: CoordinatorLayout,
                               appBarLayout: AppBarLayout,
                               layoutDirection: Int): Boolean {
        val isHandled: Boolean = super.onLayoutChild(parent, appBarLayout, layoutDirection)

        appBarLayout.addOnOffsetChangedListener { bar, offset ->
            val totalScrollRange = bar.totalScrollRange

            val progress = 1 - kotlin.math.round(
                    100 * kotlin.math.abs(offset).toFloat() / totalScrollRange) / 100

            Logger.logDebug(this.javaClass.simpleName,
                    "Scroll progress = $progress, total = $totalScrollRange, cur = $offset")

            if (progress == previousProgress) return@addOnOffsetChangedListener
            previousProgress = progress

            if (!this::fadeImage.isInitialized)
                fadeImage = parent.findViewById(R.id.blurred_picture)
            fadeImage.alpha = progress * sqrt(progress)

            if (!this::toolbar.isInitialized)
                toolbar = parent.findViewById(R.id.toolbar)
            toolbar.background.alpha = (255 - progress * 255).toInt()
        }


        return isHandled
    }

    private fun animateFadeImage(fadeImageView: FadingImageView, progress: Float) {
        val toSize = Utils.dp(fadeImageView.context, 300) * progress
        val toMargin = -1 * Utils.dp(fadeImageView.context, 90) * progress

        val params = fadeImageView.layoutParams as ViewGroup.MarginLayoutParams
        params.height = toSize.toInt()
        params.width = toSize.toInt()

        params.leftMargin = toMargin.toInt()

        fadeImageView.requestLayout()
    }
}