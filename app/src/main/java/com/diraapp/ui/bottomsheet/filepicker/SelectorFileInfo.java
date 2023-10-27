package com.diraapp.ui.bottomsheet.filepicker;

import com.diraapp.storage.DiraMediaInfo;

public class SelectorFileInfo extends DiraMediaInfo {

    private boolean isSelected;

    public SelectorFileInfo(String name, String filePath, String mimeType) {
        super(name, filePath, mimeType);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
