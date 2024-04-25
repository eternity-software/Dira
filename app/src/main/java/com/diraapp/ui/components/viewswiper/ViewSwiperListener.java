package com.diraapp.ui.components.viewswiper;

import androidx.recyclerview.widget.RecyclerView;

public interface ViewSwiperListener {

    default void onSwiped(int position) {
    }

    default void onScrollStateChanged(boolean canScroll) {
    }

    boolean denySwipe(RecyclerView.ViewHolder viewHolder);

}
