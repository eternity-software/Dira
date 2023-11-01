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
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.storage.attachments.SaveAttachmentTask;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.DiraActivityListener;
import com.diraapp.ui.waterfalls.WaterfallBalancer;
import com.diraapp.utils.Logger;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImagePreview extends RelativeLayout implements WaterfallImageView, DiraActivityListener {

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

    private static int bitmapCounter = 0;

    private ExecutorService dummyThreadPool = Executors.newFixedThreadPool(2);


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

            LayoutInflater inflater = LayoutInflater.from(getContext());

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
    public void onDestroy() {
        recycleBitmap();
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

    public void loadAttachmentFile(File mediaFile) {
        if (mediaFile == null) return;
        Attachment currentAttachment = attachment;
        if (waterfallBalancer != null) {
            String type = DiraMediaInfo.MIME_TYPE_IMAGE;

            if (attachment.getAttachmentType() == AttachmentType.VIDEO)
                type = DiraMediaInfo.MIME_TYPE_VIDEO;
            fileInfo = new DiraMediaInfo("",
                    mediaFile.getPath(),
                    type);
            waterfallBalancer.add(this);
        } else {
            Logger.logDebug("ImagePreview", "No waterfallBalancer");
//            DiraActivity.runGlobalBackground(() -> {
//                Bitmap bitmap = AppStorage.getBitmapFromPath(mediaFile.getPath(), getContext());
//                new Handler(Looper.getMainLooper()).post(() -> {
//                    if (attachment == currentAttachment) {
//
//                        setImageBitmap(bitmap);
//                        isMainImageLoaded = true;
//                    }
//                });
//            });
        }


    }

    public void setImageBitmap(Bitmap bitmap) {
        Logger.logDebug("ImagePreviewView", "Bitmap updated (" + bitmapCounter++ + " )");
        DiraActivity.runOnMainThread(() -> {
            recycleBitmap();
            loadedBitmap = bitmap;
            imageView.setImageBitmap(bitmap);
        });

    }

    private void recycleBitmap() {
        if (loadedBitmap != null) {
            if (!loadedBitmap.isRecycled()) loadedBitmap.recycle();
            loadedBitmap = null;

        }
    }


    public void prepareForAttachment(Attachment attachment, Room room, Runnable onImageReady) {
        if (attachment == null) return;
        // if (this.attachment == attachment) return;

        this.onReady = onImageReady;
        this.room = room;
        isMainImageLoaded = false;
        this.attachment = attachment;
      //  setImageBitmap(null);

        loadDummyBitmap(attachment);

        DiraActivity.runGlobalBackground(() -> {
            if (this.attachment != attachment | isMainImageLoaded) return;


            Bitmap previewBitmap = attachment.getBitmapPreview();
            if (previewBitmap != null) {
                DiraActivity.runOnMainThread(() -> {
                    if (!isMainImageLoaded) {
                        setImageBitmap(previewBitmap);
                    } else {
                        previewBitmap.recycle();
                    }


                });
            }

        });
    }

    public void showOverlay(File file, Attachment attachment) {
        if (file != null) {

            if (!AttachmentDownloader.isAttachmentSaving(attachment)) {
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
            showLoadingButton(attachment, AttachmentDownloader.isAttachmentSaving(attachment));

        }
    }

    public void displayTrash() {
        overlay.setVisibility(VISIBLE);
        progressBar.setVisibility(GONE);
        sizeTextView.setVisibility(GONE);
        downloadButton.setImageDrawable(getContext().getDrawable(R.drawable.ic_trash));
    }

    public void loadDummyBitmap(Attachment attachment) {
        dummyThreadPool.execute(() -> {
            float scale = attachment.calculateWidthScale(108);

            int width = (int) (attachment.getWidth() * scale);
            int height = (int) (attachment.getHeight() * scale);

            System.out.println(attachment + " " + attachment.getHeight());
            final Bitmap dummyBitmap = Bitmap.createBitmap(width,
                    height,
                    Bitmap.Config.ALPHA_8);

            System.out.println("Dummy " + dummyBitmap.getHeight());

            System.out.println("Dummy set!");
            DiraActivity.runOnMainThread(() -> {
                if (isMainImageLoaded | attachment != this.attachment) return;
                setImageBitmap(dummyBitmap);
            });

        });
    }

    public void hideOverlay() {
        if (!isInitialized) return;
        overlay.setVisibility(GONE);
    }

    private void showLoadingButton(Attachment attachment, boolean isLoading) {
        overlay.setVisibility(VISIBLE);
        downloadButton.setImageDrawable(getContext().getDrawable(R.drawable.ic_download));
        sizeTextView.setVisibility(VISIBLE);
        sizeTextView.setText(AppStorage.getStringSize(attachment.getSize()) +
                getContext().getString(R.string.attachment_in_queue));
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
                AttachmentDownloader.saveAttachmentAsync(saveAttachmentTask, room.getServerAddress());

                showLoadingButton(attachment, true);
            });
            downloadButton.setImageDrawable(getContext().getDrawable(R.drawable.ic_download));
        }
    }

    public void setDownloadPercent(int percent) {
        DiraActivity.runOnMainThread(() -> {
            sizeTextView.setText(AppStorage.getStringSize(attachment.getSize()) + "(" + percent + "%" + ")");
        });

    }

    public boolean isMainImageLoaded() {
        return isMainImageLoaded;
    }

    @Override
    public void onImageBind(Bitmap bitmap) {


        isMainImageLoaded = true;


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

        progressBar.setVisibility(GONE);
    }

    public void detach() {
        isMainImageLoaded = false;
        setImageBitmap(null);
        loadDummyBitmap(attachment);
    }

    public void attach() {
        if (attachment == null | fileInfo == null) return;
        prepareForAttachment(attachment, room, onReady);

        loadAttachmentFile(new File(fileInfo.getFilePath()));
    }

    @Override
    public DiraMediaInfo getFileInfo() {
        return fileInfo;
    }
}
