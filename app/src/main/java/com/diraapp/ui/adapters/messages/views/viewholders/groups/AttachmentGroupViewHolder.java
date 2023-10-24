package com.diraapp.ui.adapters.messages.views.viewholders.groups;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.adapters.messages.views.viewholders.AttachmentViewHolder;
import com.diraapp.ui.components.HeightLimitedCardView;
import com.diraapp.ui.components.ImagePreview;
import com.diraapp.utils.android.DeviceUtils;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AttachmentGroupViewHolder extends AttachmentViewHolder {


    LinearLayout mother;

    LinearLayout firstLayer ;
    LinearLayout secondLayer ;
    LinearLayout thirdLayer ;

    boolean isVerticalLayout = false;

    private List<ImagePreview> imagePreviewList = new ArrayList<>();
    public AttachmentGroupViewHolder(@NonNull ViewGroup itemView,
                                     MessageAdapterContract messageAdapterContract,
                                     ViewHolderManagerContract viewHolderManagerContract,
                                     boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);

    }

    @Override
    protected void postInflate() {
        super.postInflate();

        HeightLimitedCardView cardView = new HeightLimitedCardView(itemView.getContext());
        cardView.setMaxHeight(DeviceUtils.dpToPx(400f, rootView.getContext()));
        cardView.setCardElevation(0f);
        cardView.setRadius(DeviceUtils.dpToPx(14f, rootView.getContext()));
        mother = generateLinearLayout();
        firstLayer = generateLinearLayout();
        secondLayer = generateLinearLayout();
        thirdLayer = generateLinearLayout();

        mother.addView(firstLayer);
        mother.addView(secondLayer);
        mother.addView(thirdLayer);
        cardView.addView(mother);

        postInflatedViewsContainer.addView(cardView);
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);

        imagePreviewList.clear();

        firstLayer.removeAllViews();
        secondLayer.removeAllViews();
        thirdLayer.removeAllViews();


        secondLayer.setVisibility(View.GONE);
        thirdLayer.setVisibility(View.GONE);

        int attachmentCount = message.getAttachments().size();

        int attachmentIndex = 0;
        for(Attachment attachment : message.getAttachments())
        {
            ImagePreview imagePreview = generateImagePreview();
            imagePreview.setAttachment(attachment, getMessageAdapterContract().getRoom(),
                    AttachmentsStorage.getFileFromAttachment(attachment, itemView.getContext(),
                            getMessageAdapterContract().getRoom().getSecretName()), null);

            if(attachmentIndex == 0 |
                    (attachmentCount > 3 && attachmentIndex < 3 && !isVerticalLayout))
            {
                firstLayer.addView(imagePreview);
            } else if (attachmentIndex < 3 |
                    (attachmentCount > 6 && attachmentIndex < 5 && isVerticalLayout) |
                    (!isVerticalLayout && attachmentCount > 3 && attachmentIndex < 6)) {
                secondLayer.addView(imagePreview);
                secondLayer.setVisibility(View.VISIBLE);
            }
            else if (attachmentIndex < 10) {
                thirdLayer.addView(imagePreview);

                thirdLayer.setVisibility(View.VISIBLE);
            }
            if(attachmentIndex == 0)
            {
                if(attachment.getWidth() / (float) attachment.getHeight() > 1.5f)
                {
                    isVerticalLayout = true;
                    mother.setOrientation(LinearLayout.VERTICAL);
                    firstLayer.setOrientation(LinearLayout.HORIZONTAL);
                    secondLayer.setOrientation(LinearLayout.HORIZONTAL);
                    thirdLayer.setOrientation(LinearLayout.HORIZONTAL);

                }
                else
                {
                    isVerticalLayout = false;
                    mother.setOrientation(LinearLayout.HORIZONTAL);
                    firstLayer.setOrientation(LinearLayout.VERTICAL);
                    secondLayer.setOrientation(LinearLayout.VERTICAL);
                    thirdLayer.setOrientation(LinearLayout.VERTICAL);
                }
            }
            attachmentIndex++;
            File currentMediaFile = AttachmentsStorage.getFileFromAttachment(attachment,
                    itemView.getContext(), message.getRoomSecret());
            if (!AttachmentsStorage.isAttachmentSaving(attachment))
                onAttachmentLoaded(attachment, currentMediaFile, message);

        }
        firstLayer.requestLayout();
        secondLayer.requestLayout();
        thirdLayer.requestLayout();
        mother.requestLayout();





    }

    private ImagePreview generateImagePreview()
    {
        ImagePreview imagePreview = new ImagePreview(itemView.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        imagePreview.setLayoutParams(params);

        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        imagePreview.getImageView().setLayoutParams(imageParams);

        imagePreview.getImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);

        imagePreviewList.add(imagePreview);
        return imagePreview;
    }

    private LinearLayout generateLinearLayout()
    {
        LinearLayout mom = new LinearLayout(itemView.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        mom.setLayoutParams(params);
        return mom;
    }

    @Override
    public void onAttachmentLoaded(Attachment attachment, File file, Message message) {

        for(ImagePreview imagePreview : new ArrayList<>(imagePreviewList))
        {
            if(imagePreview.getAttachment() != null && attachment != null) {
                if (imagePreview.getAttachment().getFileUrl().equals(attachment.getFileUrl())) {
                    imagePreview.hideDownloadOverlay();
                    imagePreview.setAttachment(attachment, getMessageAdapterContract().getRoom(),
                            AttachmentsStorage.getFileFromAttachment(attachment, itemView.getContext(),
                                    getMessageAdapterContract().getRoom().getSecretName()), null);
                    imagePreview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getMessageAdapterContract().preparePreviewActivity(file.getPath(),
                                    attachment.getAttachmentType() == AttachmentType.VIDEO,
                                    imagePreview.getLoadedBitmap(), imagePreview).start();
                        }
                    });
                }
            }
        }

    }

    @Override
    public void onLoadFailed() {

    }
}
