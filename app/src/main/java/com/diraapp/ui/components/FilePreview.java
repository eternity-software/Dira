package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.ui.bottomsheet.filepicker.SelectorFileInfo;


public class FilePreview extends RelativeLayout {

    private View rootView;
    private SelectorFileInfo selectorFileInfo;
    private FileParingImageView fileParingImageView;
    private RelativeLayout videoInfoView;
    private TextView durationView;
    private TextView selectionTextButton;
    private View selectionTextContainer;
    private boolean isInitialized = false;

    public FilePreview(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void initComponent() {

        if (!isInitialized) {

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rootView = inflater.inflate(R.layout.file_preview, this);
            fileParingImageView = findViewById(R.id.fileImageView);
            videoInfoView = findViewById(R.id.videoInfo);
            durationView = findViewById(R.id.durationView);
            selectionTextButton = findViewById(R.id.select_button);
            selectionTextContainer = findViewById(R.id.select_button_container);
            isInitialized = true;
        }


    }

    public void updateUi(boolean isSelected, int position)
    {
        position++;
        if(isSelected)
        {
            selectionTextButton.getBackground().setColorFilter(Theme.getColor(getContext(), R.color.accent_dark), PorterDuff.Mode.SRC_ATOP);
            selectionTextButton.setText(String.valueOf(position));
        }
        else
        {
            selectionTextButton.setText("");
            selectionTextButton.setBackground(getContext().getDrawable(R.drawable.circle_unselected));
        }
    }

    public TextView getSelectionTextButton() {
        return selectionTextButton;
    }

    public View getSelectionTextContainer() {
        return selectionTextContainer;
    }

    public void setSelectionTextButton(TextView selectionTextButton) {
        this.selectionTextButton = selectionTextButton;
    }

    public void appearContorllers() {
        videoInfoView.setVisibility(VISIBLE);
        videoInfoView.setAlpha(0f);
        videoInfoView.animate().alpha(1f).setDuration(200).start();
    }

    public FileParingImageView getFileParingImageView() {
        initComponent();
        return fileParingImageView;
    }

    public void setSubtitle(String text) {

        durationView.setText(text);
    }

    public SelectorFileInfo getFileInfo() {
        return selectorFileInfo;
    }

    public void setFileInfo(SelectorFileInfo selectorFileInfo) {
        this.selectorFileInfo = selectorFileInfo;
        initComponent();
        fileParingImageView.setFileInfo(selectorFileInfo);
        if (selectorFileInfo.isVideo()) {
            videoInfoView.setVisibility(VISIBLE);
            setSubtitle("");
        } else {
            videoInfoView.setVisibility(INVISIBLE);
        }
    }
}
