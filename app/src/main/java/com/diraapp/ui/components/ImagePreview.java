package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.diraapp.BuildConfig;
import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Room;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.DiraMediaInfo;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.storage.attachments.SaveAttachmentTask;
import com.diraapp.ui.waterfalls.WaterfallBalancer;
import com.diraapp.ui.activities.DiraActivity;

import java.io.File;

public class ImagePreview extends RelativeLayout implements WaterfallImageView {

    private View rootView;
    private ImageView imageView;
    private TextView sizeTextView;
    private boolean isInitialized = false;
    private View overlay;
    private View progressBar;
    private ImageView downloadButton;

    private Attachment attachment;

    private boolean isMainImageLoaded = false;

    private Room room;

    private Bitmap loadedBitmap;

    private WaterfallBalancer waterfallBalancer;

    private DiraMediaInfo fileInfo;

    private Runnable onReady;


    public ImagePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initComponent();
    }

    public ImagePreview(Context context) {
        super(context);
    }

    public ImagePreview(Context context, WaterfallBalancer waterfallBalancer) {
        super(context);
        this.waterfallBalancer = waterfallBalancer;
        initComponent();
    }

    public void setWaterfallBalancer(WaterfallBalancer waterfallBalancer) {
        this.waterfallBalancer = waterfallBalancer;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void initComponent() {

        if (!isInitialized) {

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rootView = inflater.inflate(R.layout.preview_image, this);
            imageView = findViewById(R.id.preview_image);
            sizeTextView = findViewById(R.id.size_text);
            overlay = findViewById(R.id.download_overlay);
            progressBar = findViewById(R.id.progress_bar);
            downloadButton = findViewById(R.id.button_download);
            isInitialized = true;
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
        downloadButton.setOnClickListener(l);
    }

    public Bitmap getLoadedBitmap() {
        return loadedBitmap;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImage(File imageFile) {
        Attachment currentAttachment = attachment;
        if (waterfallBalancer != null) {
            String type = DiraMediaInfo.MIME_TYPE_IMAGE;

            if (attachment.getAttachmentType() == AttachmentType.VIDEO) type = DiraMediaInfo.MIME_TYPE_VIDEO;
            fileInfo = new DiraMediaInfo("",
                    AttachmentsStorage.getFileFromAttachment(attachment, getContext(),
                            room.getSecretName()).getPath(),
                    type);
            waterfallBalancer.add(this);
        } else {
            DiraActivity.runGlobalBackground(() -> {
                if (imageFile == null) return;
                Bitmap bitmap = AppStorage.getBitmapFromPath(imageFile.getPath(), getContext());
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (attachment == currentAttachment) {
                        loadedBitmap = bitmap;
                        setImageBitmap(bitmap);
                        isMainImageLoaded = true;
                    }
                });
            });
        }


    }

    private void setImageBitmap(Bitmap bitmap) {
        DiraActivity.runOnMainThread(() -> {
            imageView.setImageBitmap(bitmap);
            loadedBitmap = bitmap;
        });

    }


    public void setAttachment(Attachment attachment, Room room, File file, Runnable onReady) {
        if (attachment == null) return;
        if (this.attachment == attachment) return;

        this.onReady = onReady;
        this.room = room;
        isMainImageLoaded = false;
        this.attachment = attachment;
        overlay.setVisibility(GONE);
        imageView.setImageBitmap(null);

        if (waterfallBalancer != null && file != null) {


            String type = DiraMediaInfo.MIME_TYPE_IMAGE;

            if (attachment.getAttachmentType() == AttachmentType.VIDEO)
                type = DiraMediaInfo.MIME_TYPE_VIDEO;
            fileInfo = new DiraMediaInfo("", file.getPath(),
                    type);
            waterfallBalancer.add(this);
        }

        DiraActivity.runGlobalBackground(() -> {
            if (this.attachment != attachment | isMainImageLoaded) return;

            final Bitmap dummyBitmap = Bitmap.createBitmap(attachment.getWidth(),
                    attachment.getHeight(),
                    Bitmap.Config.ARGB_8888);


            DiraActivity.runOnMainThread(() -> {
                if (this.attachment != attachment | isMainImageLoaded) return;
                imageView.setImageBitmap(dummyBitmap);
                Bitmap previewBitmap = attachment.getBitmapPreview();
                if (previewBitmap != null) {
                    DiraActivity.runOnMainThread(() -> {
                        if (!isMainImageLoaded)
                            imageView.setImageBitmap(previewBitmap);
                    });
                }
                if (file != null) {

                    if (!AttachmentsStorage.isAttachmentSaving(attachment)) {
                        if (attachment.getAttachmentType() == AttachmentType.VIDEO) {
                            overlay.setVisibility(VISIBLE);
                            progressBar.setVisibility(GONE);
                            sizeTextView.setVisibility(GONE);
                            downloadButton.setImageDrawable(getContext().getDrawable(R.drawable.ic_play));
                        }
                    } else {

                        // attachment downloading in progress
                        showLoadingButton(attachment, true);
                    }
                } else {

                    // attachment not loaded
                    showLoadingButton(attachment, AttachmentsStorage.isAttachmentSaving(attachment));

                }
            });


        });
    }

    public void displayTrash() {
        overlay.setVisibility(VISIBLE);
        progressBar.setVisibility(GONE);
        sizeTextView.setVisibility(GONE);
        downloadButton.setImageDrawable(getContext().getDrawable(R.drawable.ic_trash));
    }

    public void hideDownloadOverlay() {
        if (!isInitialized) return;
        overlay.setVisibility(GONE);
    }

    private void showLoadingButton(Attachment attachment, boolean isLoading) {
        overlay.setVisibility(VISIBLE);
        downloadButton.setImageDrawable(getContext().getDrawable(R.drawable.ic_download));
        sizeTextView.setVisibility(VISIBLE);
        sizeTextView.setText(AppStorage.getStringSize(attachment.getSize()));
        if (isLoading) {
            progressBar.setVisibility(VISIBLE);
            downloadButton.setImageBitmap(null);
            downloadButton.setOnClickListener(v -> {
            });
        } else {

            progressBar.setVisibility(GONE);
            downloadButton.setOnClickListener(v -> {
                SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(getContext(), false,
                        attachment, room.getSecretName());
                AttachmentsStorage.saveAttachmentAsync(saveAttachmentTask, room.getServerAddress());

                showLoadingButton(attachment, true);
            });
            downloadButton.setImageDrawable(getContext().getDrawable(R.drawable.ic_download));
        }
    }

    @Override
    public void onImageBind(Bitmap bitmap) {


        isMainImageLoaded = true;
        loadedBitmap = bitmap;

        new Handler(Looper.getMainLooper()).post(() -> {
            try {


                try {
                    if (onReady != null)
                        onReady.run();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (BuildConfig.DEBUG) {
                    //showDebugInfo();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void showDebugInfo() {
        overlay.setVisibility(VISIBLE);
        sizeTextView.setText("DEBUG INFO: " + loadedBitmap.getWidth() + "x" + loadedBitmap.getHeight() + ", aid: " + attachment.getId() + "\n" +
                "VIEW SIZE: " + getMeasuredWidth() + "x" + getMeasuredHeight() + "\n" +
                "REAL SIZE: " + AppStorage.getStringSize(new File(fileInfo.getFilePath()).length()) + ", " +
                "ATT. SIZE: " + AppStorage.getStringSize(attachment.getSize()));
        downloadButton.setImageDrawable(getContext().getDrawable(R.drawable.ic_info));
        progressBar.setVisibility(GONE);
    }

    @Override
    public DiraMediaInfo getFileInfo() {
        return fileInfo;
    }
}
