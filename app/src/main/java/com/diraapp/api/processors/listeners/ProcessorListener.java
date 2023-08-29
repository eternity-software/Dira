package com.diraapp.api.processors.listeners;

public interface ProcessorListener {
    /**
     * Called when count of background connections has changed
     *
     * @param percentOpened
     */
    void onSocketsCountChange(float percentOpened);
}
