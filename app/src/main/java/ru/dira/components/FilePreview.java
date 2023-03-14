package ru.dira.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ru.dira.R;
import ru.dira.bottomsheet.filepicker.FileInfo;


public class FilePreview extends RelativeLayout {

    private View rootView;
    private FileInfo fileInfo;
    private FileParingImageView fileParingImageView;
    private RelativeLayout videoInfoView;
    private TextView durationView;
    private boolean isInitialized = false;

    public FilePreview(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void initComponent() {

        if(!isInitialized) {

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rootView = inflater.inflate(R.layout.file_preview, this);
            fileParingImageView = findViewById(R.id.fileImageView);
            videoInfoView = findViewById(R.id.videoInfo);
            durationView = findViewById(R.id.durationView);
            isInitialized = true;
        }


    }

    public void appearContorllers()
    {
        videoInfoView.setVisibility(VISIBLE);
        videoInfoView.setAlpha(0f);
        videoInfoView.animate().alpha(1f).setDuration(200).start();
    }

    public FileParingImageView getFileParingImageView() {
        initComponent();
        return fileParingImageView;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        initComponent();
        fileParingImageView.setFileInfo(fileInfo);
        if(fileInfo.isVideo())
        {
            videoInfoView.setVisibility(VISIBLE);
            setSubtitle("");
        }
        else
        {
            videoInfoView.setVisibility(INVISIBLE);
        }
    }

    public void setSubtitle(String text)
    {

        durationView.setText(text);
    }



    public FileInfo getFileInfo() {
        return fileInfo;
    }
}
