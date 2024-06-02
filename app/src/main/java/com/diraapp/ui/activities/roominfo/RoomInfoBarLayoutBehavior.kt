package com.diraapp.ui.activities.roominfo

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.diraapp.R
import com.diraapp.ui.components.FadingImageView
import com.diraapp.utils.Logger
import com.google.android.material.appbar.AppBarLayout
import com.masoudss.lib.utils.Utils
import kotlin.math.pow

class RoomInfoBarLayoutBehavior: AppBarLayout.Behavior {

    private var previousProgress: Float = 0.0F

    private lateinit var fadeImage: FadingImageView

    private lateinit var toolbar: Toolbar

    private lateinit var roomName: TextView

    private lateinit var roomImageCard: CardView

    constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet)

    override fun onLayoutChild(parent: CoordinatorLayout,
                               appBarLayout: AppBarLayout,
                               layoutDirection: Int): Boolean {
        val isHandled: Boolean = super.onLayoutChild(parent, appBarLayout, layoutDirection)

        appBarLayout.addOnOffsetChangedListener { bar, offset ->
            val totalScrollRange = bar.totalScrollRange

            val progress = 1 - kotlin.math.round(
                    500 * kotlin.math.abs(offset).toFloat() / totalScrollRange) / 500

//            Logger.logDebug(this.javaClass.simpleName,
//                    "Scroll progress = $progress, total = $totalScrollRange, cur = $offset")

            if (progress == previousProgress) return@addOnOffsetChangedListener
            previousProgress = progress

            animateFading(parent, progress)

            animateBarAlpha(parent, progress)
        }


        return isHandled
    }

    private fun animateFading(parent: CoordinatorLayout, progress: Float) {
        if (!this::fadeImage.isInitialized)
            fadeImage = parent.findViewById(R.id.blurred_picture)


        fadeImage.alpha = progress.toDouble().pow(1.3).toFloat()
    }

    private fun animateBarAlpha(parent: CoordinatorLayout, progress: Float) {
        val pr = progress.toDouble().pow(1.4)
        if (!this::toolbar.isInitialized) {
            toolbar = parent.findViewById(R.id.toolbar)

            roomName = parent.findViewById(R.id.room_name_bar)

            roomImageCard = parent.findViewById(R.id.room_picture_bar_card)
        }

        val barAlpha = (255 - pr * 255).toInt()
        toolbar.background.alpha = barAlpha

        roomName.alpha = (1 - pr).toFloat()
        roomImageCard.alpha = (1 - pr).toFloat()
    }

}