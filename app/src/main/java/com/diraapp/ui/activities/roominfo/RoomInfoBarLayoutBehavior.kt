package com.diraapp.ui.activities.roominfo

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.diraapp.R
import com.diraapp.ui.components.FadingImageView
import com.diraapp.utils.Logger
import com.diraapp.utils.android.DeviceUtils
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.pow

class RoomInfoBarLayoutBehavior: AppBarLayout.Behavior {

    private var previousProgress: Float = 0.0F

    private lateinit var fadeImage: FadingImageView

    private lateinit var toolbar: Toolbar

    private lateinit var roomName: TextView

    private lateinit var roomImageCard: CardView

    private lateinit var cardView: CardView

    private var cardViewAnimator = ValueAnimator.ofFloat(0f, 100f)

    private val cardViewRadius: Int

    private var isCardRound = true

    constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet) {
        cardViewRadius = DeviceUtils.dpToPx(18F, context)
    }

    override fun onLayoutChild(parent: CoordinatorLayout,
                               appBarLayout: AppBarLayout,
                               layoutDirection: Int): Boolean {
        val isHandled: Boolean = super.onLayoutChild(parent, appBarLayout, layoutDirection)

        appBarLayout.addOnOffsetChangedListener { bar, offset ->
            val totalScrollRange = bar.totalScrollRange

            val progress = 1 - kotlin.math.round(
                    500 * kotlin.math.abs(offset).toFloat() / totalScrollRange) / 500

            Logger.logDebug(this.javaClass.simpleName,
                    "Scroll progress = $progress, total = $totalScrollRange, cur = $offset")

            if (progress == previousProgress) return@addOnOffsetChangedListener
            previousProgress = progress

            animateFading(parent, progress)

            animateBarAlpha(parent, progress)

            animateCard(parent, progress)
        }


        return isHandled
    }

    private fun animateFading(parent: CoordinatorLayout, progress: Float) {
        if (!this::fadeImage.isInitialized)
            fadeImage = parent.findViewById(R.id.blurred_picture)

        fadeImage.alpha = (progress * 0.7).pow(1.3).toFloat()
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

    private fun animateCard(parent: CoordinatorLayout, progress: Float) {
        if (!this::cardView.isInitialized)
            cardView = parent.findViewById(R.id.card_view)

        // Scrolled down
        if (progress == 0F) {
            if (isCardRound) {
                if (cardViewAnimator.isRunning) cardViewAnimator.end()
                cardViewAnimator = ValueAnimator.ofFloat(0f, 100f)

                cardViewAnimator.addUpdateListener { animation ->
                    val p = animation.animatedValue as Float

                    cardView.radius = cardViewRadius * (100 - p)/100
                }

                cardViewAnimator.duration = 500
                cardViewAnimator.start()
                isCardRound = false
            }
            return
        }

        // Scrolled up
        if (isCardRound) return

        if (cardViewAnimator.isRunning) cardViewAnimator.end()
        cardViewAnimator = ValueAnimator.ofFloat(0f, 100f)

        cardViewAnimator.addUpdateListener { animation ->
            val p = animation.animatedValue as Float

            cardView.radius = cardViewRadius * p/100
        }

        cardViewAnimator.duration = 500
        cardViewAnimator.start()
        isCardRound = true
    }

}