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
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.legacy.LegacyRoomMessagesAdapter;
import com.diraapp.ui.components.dynamic.DynamicTextView;
import com.diraapp.ui.components.dynamic.ThemeImageView;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Numbers;
import com.diraapp.utils.StringFormatter;
import com.squareup.picasso.Picasso;

import java.io.File;

public class MessageReplyComponent extends FrameLayout {

    private final int messageType;

    private final boolean isSelfMessage;

    private boolean isInit = false;
    private LinearLayout replyContainer;
    private CardView replyImageCard;
    private ImageView replyImage;
    private DynamicTextView replyText, replyAuthor;


    public MessageReplyComponent(Context context, int messageType, boolean isSelfMessage) {
        super(context);
        this.messageType = messageType;
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
            } else if (attachment.getAttachmentType() == AttachmentType.VOICE) {
                text = getContext().getResources().getString(R.string.message_type_voice);
            } else if (attachment.getAttachmentType() == AttachmentType.IMAGE) {
                text = message.getText();
                if (text == null | StringFormatter.EMPTY_STRING.equals(text)) {
                    text = getContext().getResources().getString(R.string.message_type_image);
                } else {
                    replyText.setTextColor(Theme.getColor
                            (getContext(), textColorId));
                }

                File file = AttachmentsStorage.getFileFromAttachment(attachment,
                        getContext(), config.getRoom().getSecretName());

                if (file != null) {
                    Picasso.get().load(file).into(replyImage);
                    replyImageCard.setVisibility(View.VISIBLE);
                }
                showImage = true;
            }
        } else {
            text = message.getText();
            if (text == null) text = "";
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
                if (config.getReplyListener() != null) config.getReplyListener().onClicked(message);
            }
        });
    }

    private void initView() {
        if (isInit) return;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.message_reply_component, this);

        FrameLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout layout = this.findViewById(R.id.message_reply_container);
        if (messageType == LegacyRoomMessagesAdapter.VIEW_TYPE_ROOM_MESSAGE_ATTACHMENTS |
                messageType == LegacyRoomMessagesAdapter.VIEW_TYPE_ROOM_MESSAGE_MULTI_ATTACHMENTS) {
            int margin = Numbers.dpToPx(8, this.getContext());
            params.setMargins(margin / 2, (int) (1.5 * margin), 0, margin);
            layout.setLayoutParams(params);
        } else if (messageType == LegacyRoomMessagesAdapter.VIEW_TYPE_ROOM_MESSAGE_BUBBLE) {
            int padding = Numbers.dpToPx(6, this.getContext());
            layout = this.findViewById(R.id.message_reply_container);
            layout.setPadding(padding, padding, padding, padding);

            int margin = Numbers.dpToPx(4, this.getContext());
            params.setMargins(margin, margin, margin, margin);

            if (isSelfMessage) params.gravity = Gravity.RIGHT;
            layout.setLayoutParams(params);

            layout.setBackground(this.getContext().getResources().
                    getDrawable(R.drawable.rounded_accent_rectangle));
            layout.getBackground().setColorFilter(Theme.getColor(
                    this.getContext(), R.color.gray_trans), PorterDuff.Mode.SRC_ATOP);
        }

        if (isSelfMessage) {
            ((ThemeImageView) this.findViewById(R.id.message_reply_line)).setColorFilter(
                    Theme.getColor(this.getContext(), R.color.self_reply_color));
            ((TextView) this.findViewById(R.id.message_reply_author_name)).setTextColor(
                    Theme.getColor(this.getContext(), R.color.self_reply_color));
        }
        replyImage = findViewById(R.id.message_reply_image);
        replyImageCard = findViewById(R.id.message_reply_image_card);
        replyContainer = findViewById(R.id.message_reply_container);
        replyText = findViewById(R.id.message_reply_text);
        replyAuthor = findViewById(R.id.message_reply_author_name);
        isInit = true;
    }
}
