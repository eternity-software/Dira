package com.diraapp.ui.adapters.messages;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.utils.Numbers;

import java.util.ArrayList;
import java.util.List;

public class MessageSwiper extends ItemTouchHelper.Callback {

    private Activity mContext;

    private Drawable mReplyIcon;
    private Drawable mReplyIconBackground;

    private RecyclerView.ViewHolder mCurrentViewHolder;
    private View mView;

    private float mDx = 0f;

    private float mReplyButtonProgress = 0f;
    private long mLastReplyButtonAnimationTime = 0;

    private boolean mSwipeBack = false;
    private boolean mIsVibrating = false;
    private boolean mStartTracking = false;
    private boolean sss = false;

    private int mBackgroundColor = 0x20606060;

    private int mReplyBackgroundOffset = 18;

    private int mReplyIconXOffset = 12;
    private int mReplyIconYOffset = 11;
    private List<MessageSwipingListener> listeners = new ArrayList<>();


    public MessageSwiper(Activity context) {
        mContext = context;


        mReplyIcon = mContext.getResources().getDrawable(R.drawable.ic_reply);
        mReplyIconBackground = mContext.getResources().getDrawable(R.drawable.light_round);
    }


    public MessageSwiper(Activity context, int replyIcon, int replyIconBackground, int backgroundColor) {
        mContext = context;

        mReplyIcon = mContext.getResources().getDrawable(replyIcon);
        mReplyIconBackground = mContext.getResources().getDrawable(replyIconBackground);
        mBackgroundColor = backgroundColor;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        mView = viewHolder.itemView;
        return ItemTouchHelper.Callback.makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {

        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {


        // mStartTracking = true;
       // if (((ViewHolder) viewHolder).roomUpdatesLayout != null) return;
        dX = dX / 2;
        int maxOffset = -recyclerView.getWidth() / 5;
        if (dX < maxOffset) dX = maxOffset;
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);



        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            setTouchListener(recyclerView, viewHolder);
        }
        mCurrentViewHolder = viewHolder;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListener(RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mSwipeBack = true;
                } else {
                    mSwipeBack = false;
                }
                if (mSwipeBack) {
                    if (Math.abs(mView.getTranslationX()) >= convertToDp(50)) {
                        notifyListeners(viewHolder.getAdapterPosition());
                    }
                }
                return false;
            }
        });
    }

    private int convertToDp(int pixels) {
        return Numbers.pxToDp((float) pixels, mContext);
    }


    private void drawReplyButton(Canvas canvas) {
        if (mCurrentViewHolder == null) {
            return;
        }

        float translationX = mView.getTranslationX();
        long newTime = System.currentTimeMillis();
        long dt = Math.min(17, newTime - mLastReplyButtonAnimationTime);
        mLastReplyButtonAnimationTime = newTime;
        boolean showing = false;
        if (translationX >= convertToDp(30)) {
            showing = true;
        }
        if (showing) {
            if (mReplyButtonProgress < 1.0f) {
                mReplyButtonProgress += dt / 180.0f;
                if (mReplyButtonProgress > 1.0f) {
                    mReplyButtonProgress = 1.0f;
                } else {
                    mView.invalidate();
                }
            }
        } else if (translationX <= 0.0f) {
            mReplyButtonProgress = 0f;
            mStartTracking = false;
            mIsVibrating = false;
        } else {
            if (mReplyButtonProgress > 0.0f) {
                mReplyButtonProgress -= dt / 180.0f;
                if (mReplyButtonProgress < 0.1f) {
                    mReplyButtonProgress = 0f;
                }
            }
            mView.invalidate();
        }
        int alpha;
        float scale;
        if (showing) {
            if (mReplyButtonProgress <= 0.8f) {
                scale = 1.2f * (mReplyButtonProgress / 0.8f);
            } else {
                scale = 1.2f - 0.2f * ((mReplyButtonProgress - 0.8f) / 0.2f);
            }
            alpha = Math.min(255, 255 * ((int) (mReplyButtonProgress / 0.8f)));
        } else {
            scale = mReplyButtonProgress;
            alpha = Math.min(255, 255 * (int) mReplyButtonProgress);
        }
        mReplyIconBackground.setAlpha(alpha);
        mReplyIcon.setAlpha(alpha);
        if (mStartTracking) {
            if (!mIsVibrating && mView.getTranslationX() >= convertToDp(100)) {
                mView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }
            mIsVibrating = true;
        }

        int x;
        float y;
        if (mView.getTranslationX() > convertToDp(130)) {
            x = convertToDp(130) / 2;
        } else {
            x = (int) mView.getTranslationX() / 2;
        }

        y = mView.getTop() + ((float) mView.getMeasuredHeight() / 2);
        mReplyIconBackground.setColorFilter(mBackgroundColor, PorterDuff.Mode.MULTIPLY);

        mReplyIconBackground.setBounds(new Rect(
                (int) (x - convertToDp(mReplyBackgroundOffset) * scale),
                (int) (y - convertToDp(mReplyBackgroundOffset) * scale),
                (int) (x + convertToDp(mReplyBackgroundOffset) * scale),
                (int) (y + convertToDp(mReplyBackgroundOffset) * scale)
        ));
        mReplyIconBackground.draw(canvas);

        mReplyIcon.setBounds(new Rect(
                (int) (x - convertToDp(mReplyIconXOffset) * scale),
                (int) (y - convertToDp(mReplyIconYOffset) * scale),
                (int) (x + convertToDp(mReplyIconXOffset) * scale),
                (int) (y + convertToDp(mReplyIconYOffset) * scale)
        ));
        mReplyIcon.draw(canvas);

        mReplyIconBackground.setAlpha(255);
        mReplyIcon.setAlpha(255);
    }

    private void notifyListeners(int position) {
        for (MessageSwipingListener listener : listeners) {
            listener.onMessageSwiped(position);
        }
    }

    public void addListener(MessageSwipingListener listener) {
        listeners.add(listener);
    }

    public interface MessageSwipingListener {

        void onMessageSwiped(int position);
    }
}
