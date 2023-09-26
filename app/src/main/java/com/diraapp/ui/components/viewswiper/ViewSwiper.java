package com.diraapp.ui.components.viewswiper;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.utils.DiraVibrator;
import com.diraapp.utils.Logger;
import com.diraapp.utils.Numbers;

public class ViewSwiper {


    public static final int DEFAULT_DEATH_ZONE_DP = 20;
    public static final float SWIPE_TRIGGER_PERCENT = 0.1f;

    private RecyclerView view;

    private boolean isDown = false;
    private boolean canScroll = true;
    private boolean isSwiped = false;

    private float downY, downX;

    private View focusedView = null;
    private View downView = null;

    private ViewSwiperListener viewSwiperListener;


    public ViewSwiper(RecyclerView recyclerView) {
        setView(recyclerView);
    }

    public void setViewSwiperListener(ViewSwiperListener viewSwiperListener) {
        this.viewSwiperListener = viewSwiperListener;
    }

    private void setView(RecyclerView view) {
        this.view = view;
        setSwipeListener();
    }

    public boolean onMotionEvent( RecyclerView rv, MotionEvent event)
    {
        Logger.logDebug(getClass().getSimpleName(), "event " + event.getAction());
        View child = rv.findChildViewUnder(event.getX(), event.getY());
        if (child == null && downView == null) return false;

        if(child == null) child = downView;

        if(isDown && child != downView) child = downView;


        RecyclerView.ViewHolder viewHolder = rv.findContainingViewHolder(child);

        if(viewHolder == null) return false;

        int position = viewHolder.getAdapterPosition();



        if(focusedView != null && child != focusedView) focusedView.animate().x(0)
                .setInterpolator(new DecelerateInterpolator(2f)).setDuration(200);

        float k = 2.0f;

        float deltaY = (downY - event.getY());
        float deltaX = (downX - event.getRawX()) - Numbers.dpToPx(DEFAULT_DEATH_ZONE_DP, rv.getContext());

        if (deltaX < 0) deltaX = 0;

        Logger.logDebug(getClass().getSimpleName(), "deltaX=" + deltaX);

        boolean isIntercept = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downView = child;
                isDown = true;
                downY = event.getY();
                downX = event.getRawX();
                isSwiped = false;

                break;
            case MotionEvent.ACTION_MOVE:
                if (isDown) {


                    if(deltaX != 0)
                    {
                        float newX = (child.getX() - deltaX) / k;

                        downView.setX(newX);
                        notifyScrollStateChanged(false);
                        if(deltaX > rv.getWidth() * SWIPE_TRIGGER_PERCENT && !isSwiped)
                        {
                            isSwiped = true;
                            DiraVibrator.vibrateOneTime(rv.getContext());
                            notifyViewSwiped(position);
                        }

                        if(deltaX < rv.getWidth() * SWIPE_TRIGGER_PERCENT * 0.8 && isSwiped)
                        {
                            isSwiped = false;
                        }

                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_HOVER_EXIT:

                if(isDown)
                {
                    isDown = false;
                    downView.animate().x(0).setInterpolator(new DecelerateInterpolator(2f))
                            .setDuration(200);
                    notifyScrollStateChanged(true);

                }





        }

        return isIntercept;
    }

    
    private void notifyViewSwiped(int position)
    {

        if(viewSwiperListener != null) {
            viewSwiperListener.onSwiped(position);
        }
    }

    private void notifyScrollStateChanged(boolean canScroll)
    {
        this.canScroll = canScroll;
        if(viewSwiperListener != null) {
            viewSwiperListener.onScrollStateChanged(canScroll);
        }
    }

    private void setSwipeListener() {
        view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent event) {
               onMotionEvent(rv, event);
               return !canScroll;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent event) {
                onMotionEvent(rv, event);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

    }

}
