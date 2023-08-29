package com.diraapp.ui.activities;

public interface DiraActivityListener {

    default void onCreate(){};
    default void onResume(){};
    default void onDestroy(){};
    default void onPause(){};
}
