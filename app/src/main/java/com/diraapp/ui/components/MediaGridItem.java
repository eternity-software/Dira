package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.ui.bottomsheet.filepicker.SelectorFileInfo;


public class MediaGridItem extends RelativeLayout implements WaterfallImageView {

    private View rootView;
    private SelectorFileInfo diraMediaInfo;
    private FileParingImageView fileParingImageView;
    private RelativeLayout videoInfoView;
    private TextView durationView;
    private TextView selectionTextButton;
    private View selectionTextContainer;
    private boolean isInitialized = false;

    public MediaGridItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initComponent();
    }

    public void initComponent() {

        if (!isInitialized) {
            isInitialized = true;
            LayoutInflater inflater = LayoutInflater.from(getContext());

            rootView = inflater.inflate(R.layout.media_grid_item, this);
            fileParingImageView = findViewById(R.id.fileImageView);
            videoInfoView = findViewById(R.id.videoInfo);
            durationView = findViewById(R.id.durationView);
            selectionTextButton = findViewById(R.id.select_button);
            selectionTextContainer = findViewById(R.id.select_button_container);

        }


    }

    public void updateUi(boolean isSelected, int position) {
        position++;
        if (isSelected) {
            selectionTextButton.getBackground().setColorFilter(Theme.getColor(getContext(), R.color.accent_dark), PorterDuff.Mode.SRC_ATOP);
            selectionTextButton.setText(String.valueOf(position));
        } else {
            selectionTextButton.setText("");
            selectionTextButton.setBackground(getContext().getDrawable(R.drawable.circle_unselected));
        }
    }

    public TextView getSelectionTextButton() {
        return selectionTextButton;
    }

    public void setSelectionTextButton(TextView selectionTextButton) {
        this.selectionTextButton = selectionTextButton;
    }

    public View getSelectionTextContainer() {
        return selectionTextContainer;
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
        return diraMediaInfo;
    }

    public void setFileInfo(SelectorFileInfo diraMediaInfo) {
        this.diraMediaInfo = diraMediaInfo;
        initComponent();
        fileParingImageView.setFileInfo(diraMediaInfo);
        if (diraMediaInfo.isVideo()) {
            videoInfoView.setVisibility(VISIBLE);
            setSubtitle("");
        } else {
            videoInfoView.setVisibility(INVISIBLE);
        }
    }

    @Override
    public ImageView getImageView() {
        return getFileParingImageView();
    }
}
