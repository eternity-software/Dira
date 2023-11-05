package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.res.Theme;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.viewholders.factories.MessageHolderType;
import com.diraapp.ui.components.dynamic.DynamicTextView;
import com.diraapp.ui.components.dynamic.ThemeImageView;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.StringFormatter;
import com.diraapp.utils.android.DeviceUtils;
import com.squareup.picasso.Picasso;

import java.io.File;

public class MessageReplyComponent extends FrameLayout {

    private final MessageHolderType messageType;

    private final boolean isSelfMessage;

    private boolean isInit = false;
    private LinearLayout replyContainer;
    private CardView replyImageCard;
    private ImageView replyImage;
    private DynamicTextView replyText, replyAuthor;


    public MessageReplyComponent(Context context, int messageType, boolean isSelfMessage) {
        super(context);
        this.messageType = MessageHolderType.values()[messageType];
        this.isSelfMessage = isSelfMessage;
        initView();
    }

    public void fillMessageReply(Message message, MessageAdapterContract config) {
        if (message == null) {
            replyContainer.setVisibility(View.GONE);
            return;
        }

        String text = "";

        Attachment attachment = null;
        boolean showImage = false;
        int size = message.getAttachments().size();
        if (size > 0) {
            attachment = message.getAttachments().get(0);
        }

        int textColorId = 0;
        if (isSelfMessage) textColorId = R.color.self_message_color;
        else textColorId = R.color.message_color;

        if (attachment != null) {
            int statusTextColor = 0;
            if (isSelfMessage) statusTextColor = R.color.self_reply_color;
            else statusTextColor = R.color.message_reply_color;
            replyText.setTextColor(Theme.getColor(getContext(), statusTextColor));
            if (size > 1) {
                text = getContext().getResources().getString(R.string.message_type_attachments);
            } else if (attachment.getAttachmentType() == AttachmentType.BUBBLE) {
                text = getContext().getResources().getString(R.string.message_type_bubble);
            } else if (attachment.getAttachmentType() == AttachmentType.VIDEO) {
                text = getContext().getResources().getString(R.string.message_type_video);
            } else if (attachment.getAttachmentType() == AttachmentType.FILE) {
                text = getResources().getString(R.string.message_type_file);
            }  else if (attachment.getAttachmentType() == AttachmentType.VOICE) {
                text = getContext().getResources().getString(R.string.message_type_voice);
            } else if (attachment.getAttachmentType() == AttachmentType.IMAGE) {
                text = message.getText();
                if (text == null | StringFormatter.EMPTY_STRING.equals(text)) {
                    text = getContext().getResources().getString(R.string.message_type_image);
                } else {
                    replyText.setTextColor(Theme.getColor
                            (getContext(), textColorId));
                }

                File file = AttachmentDownloader.getFileFromAttachment(attachment,
                        getContext(), config.getRoom().getSecretName());

                if (file != null) {
                    int imageSize = DeviceUtils.dpToPx(40, getContext());
                    Picasso.get().load(file).resize(imageSize, imageSize).into(replyImage);
                    replyImageCard.setVisibility(View.VISIBLE);
                }
                showImage = true;
            }
        } else {
            text = message.getText();
            if (text == null) text = StringFormatter.EMPTY_STRING;
            replyText.setTextColor(Theme.getColor
                    (getContext(), textColorId));
        }

        if (!showImage) {
            replyImageCard.setVisibility(View.GONE);
        }

        String author = "";
        boolean isUnknown = true;
        if (message.getAuthorId().equals(config.getCacheUtils().getString(CacheUtils.ID))) {
            author = getContext().getString(R.string.you);
            isUnknown = false;
        } else if (config.getMembers() != null) {
            Member member = config.getMembers().get(message.getAuthorId());
            if (member != null) {
                author = member.getNickname();
                isUnknown = false;
            }
        }

        if (isUnknown) {
            author = getContext().getString(R.string.unknown);
        }

        replyAuthor.setText(author);
        replyText.setText(text);

        replyContainer.setVisibility(View.VISIBLE);

        replyContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (config.getReplyListener() != null)
                    config.getReplyListener().onReplyClicked(message);
            }
        });
    }

    private void initView() {
        if (isInit) return;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.message_reply_component, this);

        FrameLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        replyContainer = findViewById(R.id.message_reply_container);
        if (messageType == MessageHolderType.ROOM_SINGLE_ATTACHMENT_MESSAGE |
                messageType == MessageHolderType.SELF_SINGLE_ATTACHMENT_MESSAGE) {
            int margin = DeviceUtils.dpToPx(8, this.getContext());
            params.setMargins(margin / 2, (int) (1.5 * margin), 0, margin);
            replyContainer.setLayoutParams(params);
        } else if (messageType == MessageHolderType.ROOM_BUBBLE_MESSAGE ||
                messageType == MessageHolderType.SELF_BUBBLE_MESSAGE ||
                messageType == MessageHolderType.ROOM_EMOJI_MESSAGE ||
                messageType == MessageHolderType.SELF_EMOJI_MESSAGE) {
            int padding = DeviceUtils.dpToPx(6, this.getContext());
            replyContainer = this.findViewById(R.id.message_reply_container);
            replyContainer.setPadding(padding, padding, padding, padding);

            int margin = DeviceUtils.dpToPx(4, this.getContext());
            params.setMargins(margin, margin, margin, margin);

            if (isSelfMessage) params.gravity = Gravity.RIGHT;
            replyContainer.setLayoutParams(params);

            replyContainer.setBackground(this.getContext().getResources().
                    getDrawable(R.drawable.rounded_dark_background));
            replyContainer.getBackground().setColorFilter(Theme.getColor(
                    this.getContext(), R.color.gray_trans), PorterDuff.Mode.SRC_ATOP);
        }

        if (isSelfMessage) {
            ((ThemeImageView) this.findViewById(R.id.message_reply_line)).
                    getBackground().setColorFilter(
                            Theme.getColor(this.getContext(), R.color.self_reply_color),
                            PorterDuff.Mode.SRC_IN);
            ((TextView) this.findViewById(R.id.message_reply_author_name)).setTextColor(
                    Theme.getColor(this.getContext(), R.color.self_reply_color));
        }
        replyImage = findViewById(R.id.message_reply_image);
        replyImageCard = findViewById(R.id.message_reply_image_card);
        replyText = findViewById(R.id.message_reply_text);
        replyAuthor = findViewById(R.id.message_reply_author_name);
        isInit = true;
    }
}
