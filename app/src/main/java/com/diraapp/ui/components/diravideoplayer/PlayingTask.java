package com.diraapp.ui.components.diravideoplayer;

/**
 * An object that handles data about playing media file
 */
public class PlayingTask {
    private String sourcePath;

    public PlayingTask(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }
}
