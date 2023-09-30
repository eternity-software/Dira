package com.diraapp.ui.adapters.messages.viewholderfactories;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.UnknownViewTypeException;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.AttachmentViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.BubbleViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.EmojiMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.RoomUpdatesViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.TextMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.VoiceViewHolder;
import com.diraapp.utils.StringFormatter;

public class RoomViewHolderFactory implements BaseViewHolderFactory {

    @Override
    public BaseMessageViewHolder createViewHolder(int intType, View parent)
            throws UnknownViewTypeException {
        ViewHolderType type = ViewHolderType.values()[intType];

        switch (type){
            case ROOM_TEXT_MESSAGE:
            case SELF_TEXT_MESSAGE:
                return new TextMessageViewHolder(parent);
            case ROOM_BUBBLE_MESSAGE:
            case SELF_BUBBLE_MESSAGE:
                return new BubbleViewHolder(parent);
            case ROOM_VOICE_MESSAGE:
            case SELF_VOICE_MESSAGE:
                return new VoiceViewHolder(parent);
            case ROOM_ATTACHMENTS_MESSAGE:
            case SELF_ATTACHMENTS_MESSAGE:
                return new AttachmentViewHolder(parent);
            case ROOM_EMOJI_MESSAGE:
            case SELF_EMOJI_MESSAGE:
                return new EmojiMessageViewHolder(parent);
            case ROOM_UPDATES:
                return new RoomUpdatesViewHolder(parent);
        }
        throw new UnknownViewTypeException();
    }

    @Override
    public ViewHolderType getViewHolderType(Message message, boolean isSelfMessage)
            throws UnknownViewTypeException {
        if (message.getAuthorId() == null) {
            return ViewHolderType.ROOM_UPDATES;
        }

        if (isSelfMessage) {
            if (message.getAttachments().size() > 0) {
                Attachment attachment = message.getAttachments().get(0);
                if (attachment.getAttachmentType() == AttachmentType.VOICE) {
                    return ViewHolderType.SELF_VOICE_MESSAGE;
                } else if (attachment.getAttachmentType() == AttachmentType.BUBBLE) {
                    return ViewHolderType.SELF_BUBBLE_MESSAGE;
                }
                return ViewHolderType.SELF_ATTACHMENTS_MESSAGE;
            } else if (message.getText().length() > 0) {
                if (StringFormatter.isEmoji(message.getText()) &&
                        StringFormatter.getEmojiCount(message.getText()) < 4) {
                    return ViewHolderType.SELF_EMOJI_MESSAGE;
                }
                return ViewHolderType.SELF_TEXT_MESSAGE;
            }
        }

        if (message.getAttachments().size() > 0) {
            Attachment attachment = message.getAttachments().get(0);
            if (attachment.getAttachmentType() == AttachmentType.VOICE) {
                return ViewHolderType.ROOM_VOICE_MESSAGE;
            } else if (attachment.getAttachmentType() == AttachmentType.BUBBLE) {
                return ViewHolderType.ROOM_BUBBLE_MESSAGE;
            }
            return ViewHolderType.ROOM_ATTACHMENTS_MESSAGE;
        } else if (message.getText().length() > 0) {
            if (StringFormatter.isEmoji(message.getText()) &&
                    StringFormatter.getEmojiCount(message.getText()) < 4) {
                return ViewHolderType.ROOM_EMOJI_MESSAGE;
            }
            return ViewHolderType.ROOM_TEXT_MESSAGE;
        }
        throw new UnknownViewTypeException();
    }
}
