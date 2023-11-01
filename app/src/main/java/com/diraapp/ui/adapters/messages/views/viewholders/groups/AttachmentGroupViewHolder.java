package com.diraapp.ui.adapters.messages.views.viewholders.groups;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.res.Theme;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.adapters.messages.views.viewholders.TextMessageViewHolder;
import com.diraapp.ui.components.HeightLimitedCardView;
import com.diraapp.ui.components.ImagePreview;
import com.diraapp.utils.android.DeviceUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * AttachmentGroupViewHolder displays a gallery of images or videos and
 * sorts them by layers (mosaic layout)
 */
public class AttachmentGroupViewHolder extends TextMessageViewHolder {


    private LinearLayout layersContainer;

    private LinearLayout firstLayer, secondLayer, thirdLayer;

    /**
     * Indicates how the layers are oriented
     */
    private boolean isVerticalLayout = false;

    private List<ImagePreview> imagePreviewList = new ArrayList<>();
    private List<ImagePreview> previewImagePool = new ArrayList<>();

    private int lastPickedPoolIndex = 0;


    public AttachmentGroupViewHolder(@NonNull ViewGroup itemView,
                                     MessageAdapterContract messageAdapterContract,
                                     ViewHolderManagerContract viewHolderManagerContract,
                                     boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);

    }

    /**
     * Prepare containers to bind
     *
     * @return
     */
    @Override
    protected void postInflate() {
        super.postInflate();

        HeightLimitedCardView cardView = new HeightLimitedCardView(itemView.getContext());
        cardView.setMaxHeight(DeviceUtils.dpToPx(400f, rootView.getContext()));
        cardView.setCardElevation(0f);
        cardView.setClipChildren(true);
        cardView.setCardBackgroundColor(Theme.getColor(itemView.getContext(), R.color.gray));
        cardView.setRadius(DeviceUtils.dpToPx(14f, rootView.getContext()));

        layersContainer = generateLinearLayout();
        firstLayer = generateLinearLayout();
        secondLayer = generateLinearLayout();
        thirdLayer = generateLinearLayout();

        layersContainer.addView(firstLayer);
        layersContainer.addView(secondLayer);
        layersContainer.addView(thirdLayer);
        cardView.addView(layersContainer);

        for (int i = 0; i < 12; i++) {
            generateImagePreview();
        }

        postInflatedViewsContainer.addView(cardView);
    }

    @Override
    public void bindMessage(@NonNull Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);

        imagePreviewList.clear();

        firstLayer.removeAllViews();
        secondLayer.removeAllViews();
        thirdLayer.removeAllViews();


        secondLayer.setVisibility(View.GONE);
        thirdLayer.setVisibility(View.GONE);

        int attachmentCount = message.getAttachments().size();

        int attachmentIndex = 0;
        for (Attachment attachment : message.getAttachments()) {
            ImagePreview imagePreview = getImagePreview();
            imagePreview.prepareForAttachment(attachment,
                    getMessageAdapterContract().getRoom(),
                    null);

            if (attachmentIndex == 0 |
                    (attachmentCount > 3 && attachmentIndex < 3 && !isVerticalLayout)) {
                firstLayer.addView(imagePreview);
            } else if (attachmentIndex < 3 |
                    (attachmentCount > 6 && attachmentIndex < 5 && isVerticalLayout) |
                    (!isVerticalLayout && attachmentCount > 3 && attachmentIndex < 6)) {
                secondLayer.addView(imagePreview);
                secondLayer.setVisibility(View.VISIBLE);
            } else if (attachmentIndex < 10) {
                thirdLayer.addView(imagePreview);

                thirdLayer.setVisibility(View.VISIBLE);
            }
            if (attachmentIndex == 0) {
                if (attachment.getWidth() / (float) attachment.getHeight() > 1.5f) {
                    isVerticalLayout = true;
                    layersContainer.setOrientation(LinearLayout.VERTICAL);
                    firstLayer.setOrientation(LinearLayout.HORIZONTAL);
                    secondLayer.setOrientation(LinearLayout.HORIZONTAL);
                    thirdLayer.setOrientation(LinearLayout.HORIZONTAL);

                } else {
                    isVerticalLayout = false;
                    layersContainer.setOrientation(LinearLayout.HORIZONTAL);
                    firstLayer.setOrientation(LinearLayout.VERTICAL);
                    secondLayer.setOrientation(LinearLayout.VERTICAL);
                    thirdLayer.setOrientation(LinearLayout.VERTICAL);
                }
            }
            attachmentIndex++;
            File currentMediaFile = AttachmentDownloader.getFileFromAttachment(attachment,
                    itemView.getContext(), message.getRoomSecret());

            imagePreview.prepareForAttachment(attachment,
                    getMessageAdapterContract().getRoom(), null);

            if (currentMediaFile == null) {
                AttachmentDownloader.setDownloadHandlerForAttachment(progress -> {
                    onLoadPercentChanged(attachment, progress);
                }, attachment);
            }

            if (!AttachmentDownloader.isAttachmentSaving(attachment))
                onAttachmentLoaded(attachment, currentMediaFile, message);

        }


    }

    /**
     * Get ImagePreview from pool
     *
     * @return
     */
    private ImagePreview getImagePreview() {
        ImagePreview imagePreview = previewImagePool.get(lastPickedPoolIndex);
        if (lastPickedPoolIndex + 1 > previewImagePool.size() - 1) {
            lastPickedPoolIndex = 0;
        } else {
            lastPickedPoolIndex++;
        }
        imagePreviewList.add(imagePreview);
        return imagePreview;
    }

    /**
     * Generate item for AttachmentGroup pool
     *
     * @return
     */
    private ImagePreview generateImagePreview() {
        ImagePreview imagePreview = new ImagePreview(itemView.getContext(), getMessageAdapterContract().getWaterfallBalancer());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        imagePreview.setLayoutParams(params);

        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        imagePreview.getImageView().setLayoutParams(imageParams);
        getMessageAdapterContract().addListener(imagePreview);

        imagePreview.getImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);
        int padding = DeviceUtils.dpToPx(1f, rootView.getContext());
        imagePreview.setPadding(padding, padding, padding, padding);
        previewImagePool.add(imagePreview);
        return imagePreview;
    }

    /**
     * Generate container for AttachmentGroup layer
     *
     * @return
     */
    private LinearLayout generateLinearLayout() {
        LinearLayout mom = new LinearLayout(itemView.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        mom.setLayoutParams(params);
        return mom;


    }

    public void onLoadPercentChanged(Attachment attachment, int percent) {

        for (ImagePreview imagePreview : new ArrayList<>(imagePreviewList)) {
            if (imagePreview.getAttachment() != null && attachment != null) {
                if (imagePreview.getAttachment().getFileUrl().equals(attachment.getFileUrl())) {
                    imagePreview.setDownloadPercent(percent);
                }
            }
        }
    }


    @Override
    public void onAttachmentLoaded(Attachment attachment, File file, Message message) {

        for (ImagePreview imagePreview : new ArrayList<>(imagePreviewList)) {
            if (imagePreview.getAttachment() != null && attachment != null) {
                if (imagePreview.getAttachment().getFileUrl().equals(attachment.getFileUrl())) {
                    imagePreview.hideOverlay();
                    imagePreview.showOverlay(file, attachment);
                    imagePreview.loadAttachmentFile(file);

                    imagePreview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getMessageAdapterContract().preparePreviewActivity(file.getPath(),
                                    attachment.getAttachmentType() == AttachmentType.VIDEO,
                                    imagePreview.getLoadedBitmap(), imagePreview.getImageView()).start();
                        }
                    });
                }
            }
        }

    }

    @Override
    public void onLoadFailed(Attachment attachment) {
        if (!isInitialized) return;
        for (ImagePreview imagePreview : new ArrayList<>(imagePreviewList)) {
            if (imagePreview.getAttachment() != null && attachment != null) {
                if (imagePreview.getAttachment().getFileUrl().equals(attachment.getFileUrl())) {
                    imagePreview.displayTrash();
                }
            }
        }
    }

    @Override
    public void onViewRecycled() {
        super.onViewRecycled();
        if(!isInitialized) return;
        for(ImagePreview imagePreview : imagePreviewList)
        {
            imagePreview.detach();
        }

    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        for(ImagePreview imagePreview : imagePreviewList)
        {
            imagePreview.detach();
        }
    }

    @Override
    public void onViewAttached() {
        super.onViewAttached();
        for(ImagePreview imagePreview : imagePreviewList)
        {
            imagePreview.attach();
        }
    }
}
