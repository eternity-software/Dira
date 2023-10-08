package com.diraapp.ui.adapters.messages.views.viewholders.factories;

import android.view.ViewGroup;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.UnknownViewTypeException;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.AttachmentViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.BubbleViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.EmojiMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.RoomUpdatesViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.TextMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.VoiceViewHolder;
import com.diraapp.utils.StringFormatter;

/**
 * A factory that creating a ViewHolder from its type
 */
public class RoomViewHolderFactory implements BaseViewHolderFactory {

    @Override
    public BaseMessageViewHolder createViewHolder(int intType, ViewGroup parent, MessageAdapterContract messageAdapterContract)
            throws UnknownViewTypeException {
        MessageHolderType type = MessageHolderType.values()[intType];
        boolean isSelfMessage = type.isSelf();
        switch (type) {
            case ROOM_TEXT_MESSAGE:
            case SELF_TEXT_MESSAGE:
                return new TextMessageViewHolder(parent, messageAdapterContract, isSelfMessage);
            case ROOM_BUBBLE_MESSAGE:
            case SELF_BUBBLE_MESSAGE:
                return new BubbleViewHolder(parent, messageAdapterContract, isSelfMessage);
            case ROOM_VOICE_MESSAGE:
            case SELF_VOICE_MESSAGE:
                return new VoiceViewHolder(parent, messageAdapterContract, isSelfMessage);
            case ROOM_ATTACHMENTS_MESSAGE:
            case SELF_ATTACHMENTS_MESSAGE:
                return new AttachmentViewHolder(parent, messageAdapterContract, isSelfMessage);
            case ROOM_EMOJI_MESSAGE:
            case SELF_EMOJI_MESSAGE:
                return new EmojiMessageViewHolder(parent, messageAdapterContract, isSelfMessage);
            case ROOM_UPDATES:
                return new RoomUpdatesViewHolder(parent, messageAdapterContract);
        }
        throw new UnknownViewTypeException();
    }

    @Override
    public MessageHolderType getViewHolderType(Message message, boolean isSelfMessage)
            throws UnknownViewTypeException {
        if (message.getAuthorId() == null) {
            return MessageHolderType.ROOM_UPDATES;
        }

        if (isSelfMessage) {
            if (message.getAttachments().size() > 0) {
                Attachment attachment = message.getAttachments().get(0);
                if (attachment.getAttachmentType() == AttachmentType.VOICE) {
                    return MessageHolderType.SELF_VOICE_MESSAGE;
                } else if (attachment.getAttachmentType() == AttachmentType.BUBBLE) {
                    return MessageHolderType.SELF_BUBBLE_MESSAGE;
                }
                return MessageHolderType.SELF_ATTACHMENTS_MESSAGE;
            } else if (message.getText().length() > 0) {
                if (StringFormatter.isEmoji(message.getText()) &&
                        StringFormatter.getEmojiCount(message.getText()) < 4) {
                    return MessageHolderType.SELF_EMOJI_MESSAGE;
                }
                return MessageHolderType.SELF_TEXT_MESSAGE;
            }
        }

        if (message.getAttachments().size() > 0) {
            Attachment attachment = message.getAttachments().get(0);
            if (attachment.getAttachmentType() == AttachmentType.VOICE) {
                return MessageHolderType.ROOM_VOICE_MESSAGE;
            } else if (attachment.getAttachmentType() == AttachmentType.BUBBLE) {
                return MessageHolderType.ROOM_BUBBLE_MESSAGE;
            }
            return MessageHolderType.ROOM_ATTACHMENTS_MESSAGE;
        } else if (message.getText().length() > 0) {
            if (StringFormatter.isEmoji(message.getText()) &&
                    StringFormatter.getEmojiCount(message.getText()) < 4) {
                return MessageHolderType.ROOM_EMOJI_MESSAGE;
            }
            return MessageHolderType.ROOM_TEXT_MESSAGE;
        }
        throw new UnknownViewTypeException();
    }
}
