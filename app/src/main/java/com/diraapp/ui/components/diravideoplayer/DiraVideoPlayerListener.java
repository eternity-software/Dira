package com.diraapp.ui.components.diravideoplayer;

public interface DiraVideoPlayerListener {

    /**
     * Notify on every non-silent DiraVidePlayer state change
     *
     * @param diraVideoPlayerState Current player state
     * @return true if your task has been completed (used in addSelfDestroy method)
     */
    boolean onStateChanged(DiraVideoPlayerState diraVideoPlayerState);
}
