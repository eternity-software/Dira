package com.diraapp.ui.components.viewswiper;

public interface ViewSwiperListener {

    default void onSwiped(int position) {
    }

    default void onScrollStateChanged(boolean canScroll) {
    }

}
