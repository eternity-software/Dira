package com.diraapp.ui.bottomsheet.filepicker;

import com.diraapp.storage.DiraMediaInfo;

public class SelectorFileInfo extends DiraMediaInfo {

    private boolean isSelected;

    private String bucketName;

    public SelectorFileInfo(String name, String filePath, String mimeType) {
        super(name, filePath, mimeType);
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
