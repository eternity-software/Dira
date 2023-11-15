package com.diraapp.ui.activities.room;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.utils.android.DeviceUtils;

public class MessageItemAnimator extends DefaultItemAnimator {

    public MessageItemAnimator() {
        setMoveDuration(200);
    }

//    @Override
//    public boolean animateAdd(RecyclerView.ViewHolder holder) {
//        if (holder instanceof BaseMessageViewHolder) {
//            boolean isSelfMessage = ((BaseMessageViewHolder) holder).isSelfMessage();
//            final View view = holder.itemView;
//            float y = view.getTranslationY();
//            float x = view.getTranslationX();
//
//            if (isSelfMessage) {
//                view.setTranslationX((float) (view.getTranslationX() - view.getWidth() * 0.2));
//            }
//
//            view.setTranslationY(y + view.getHeight() * 2);
//            view.animate().translationY(y).setDuration((long) (getMoveDuration() * 1.2)).start();
//            view.animate().translationX(x).setDuration((long) (getMoveDuration() * 1.4))
//                    .setInterpolator(new DecelerateInterpolator(2f)).start();
//        }
//        return true;
//    }

    @Override
    public boolean animateAppearance(RecyclerView.ViewHolder holder, ItemHolderInfo preInfo, ItemHolderInfo postInfo) {

        if (holder instanceof BaseMessageViewHolder) {
            boolean isSelfMessage = ((BaseMessageViewHolder) holder).isSelfMessage();
            final View view = holder.itemView;
            float y = view.getTranslationY();
            float x = view.getTranslationX();

            if (isSelfMessage) {
                view.setTranslationX((float) (view.getTranslationX() - view.getWidth() * 0.2));
            }

            view.setTranslationY(y + view.getHeight() * 2);
            view.animate().translationY(y).setDuration((long) (getMoveDuration() * 1.2)).start();
            view.animate().translationX(x).setDuration((long) (getMoveDuration() * 1.4))
                    .setInterpolator(new DecelerateInterpolator(2f)).start();

            return true;
        }

        return true;
    }
}
