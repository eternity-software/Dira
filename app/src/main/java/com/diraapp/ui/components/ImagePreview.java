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

import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.Room;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.storage.attachments.SaveAttachmentTask;
import com.diraapp.ui.activities.DiraActivity;

import java.io.File;

public class ImagePreview extends RelativeLayout {

    private View rootView;
    private ImageView imageView;
    private TextView sizeTextView;
    private boolean isInitialized = false;
    private View downloadOverlay;
    private View progressBar;
    private ImageView downloadButton;

    private Attachment attachment;

    private boolean isMainImageLoaded = false;

    private Room room;

    private Bitmap loadedBitmap;

    public ImagePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initComponent();
    }

    public void initComponent() {

        if (!isInitialized) {

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rootView = inflater.inflate(R.layout.preview_image, this);
            imageView = findViewById(R.id.preview_image);
            sizeTextView = findViewById(R.id.size_text);
            downloadOverlay = findViewById(R.id.download_overlay);
            progressBar = findViewById(R.id.progress_bar);
            downloadButton = findViewById(R.id.button_download);
            isInitialized = true;
        }
    }

    public Bitmap getLoadedBitmap() {
        return loadedBitmap;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImage(File imageFile) {
        Attachment currentAttachment = attachment;
        DiraActivity.runGlobalBackground(() -> {
            Bitmap bitmap = AppStorage.getBitmapFromPath(imageFile.getPath(), getContext());
            new Handler(Looper.getMainLooper()).post(() -> {
                if (attachment == currentAttachment) {
                    loadedBitmap = bitmap;
                    imageView.setImageBitmap(bitmap);
                    isMainImageLoaded = true;
                }
            });
        });
    }

    public void setAttachment(Attachment attachment, Room room, File file, Runnable onReady) {
        this.room = room;
        isMainImageLoaded = false;
        this.attachment = attachment;
        downloadOverlay.setVisibility(GONE);
        imageView.setImageBitmap(null);
        DiraActivity.runGlobalBackground(() -> {
            Bitmap previewBitmap = attachment.getBitmapPreview();
            if (previewBitmap == null) {
                previewBitmap = Bitmap.createBitmap(attachment.getWidth(),
                        attachment.getHeight(),
                        Bitmap.Config.ARGB_8888);

            }
            Bitmap finalPreviewBitmap = previewBitmap;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (this.attachment != attachment | isMainImageLoaded) return;
                loadedBitmap = finalPreviewBitmap;
                imageView.setImageBitmap(finalPreviewBitmap);

                DiraActivity.runOnMainThread(() -> {
                    try {

                        try {
                            onReady.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (file != null) {

                            if (!AttachmentsStorage.isAttachmentSaving(attachment)) {

                            } else {

                                // attachment downloading in progress
                                setAttachmentInfo(attachment, true);
                            }
                        } else {

                            // attachment not loaded
                            setAttachmentInfo(attachment, false);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        });
    }

    private void setAttachmentInfo(Attachment attachment, boolean isLoading) {
        downloadOverlay.setVisibility(VISIBLE);
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

                setAttachmentInfo(attachment, true);
            });
            downloadButton.setImageDrawable(getContext().getDrawable(R.drawable.ic_download));
        }
    }


}
