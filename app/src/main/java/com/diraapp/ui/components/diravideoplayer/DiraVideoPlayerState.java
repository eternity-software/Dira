package com.diraapp.ui.components.diravideoplayer;

/**
 * State-list for DiraVideoPlayer
 */
public enum DiraVideoPlayerState {

    /**
     * Player has a prepared media that is playing now
     */
    PLAYING,

    /**
     * Player has a prepared media that is paused now
     */
    PAUSED,

    /**
     * Player is preparing a media task
     */
    PREPARING,

    /**
     * Player is ready to get new PlayingTask
     */
    READY,

    /**
     * Player is created, but not ready to play
     */
    IDLE,

    /**
     * Player is reset, nothing can happen
     */
    RESET
}
