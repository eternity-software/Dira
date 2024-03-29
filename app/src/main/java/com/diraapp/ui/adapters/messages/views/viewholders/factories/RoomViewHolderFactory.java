package com.diraapp.ui.adapters.messages.views.viewholders.factories;

import android.os.Build;
import android.view.ViewGroup;

import com.diraapp.BuildConfig;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.UnknownViewTypeException;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.adapters.messages.views.viewholders.EmojiMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.FileAttachmentViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.MediaViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.RoomUpdatesViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.TextMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.groups.AttachmentGroupViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.listenable.BubbleViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.listenable.VoiceViewHolder;
import com.diraapp.utils.Logger;
import com.diraapp.utils.StringFormatter;

import java.util.HashMap;

/**
 * A factory that creating a ViewHolder from its type
 */
public class RoomViewHolderFactory implements BaseViewHolderFactory {

    private final HashMap<MessageHolderType, Integer> countMap = new HashMap<>();

    public RoomViewHolderFactory() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        countMap.put(MessageHolderType.ROOM_SINGLE_ATTACHMENT_MESSAGE, 0);
        countMap.put(MessageHolderType.SELF_SINGLE_ATTACHMENT_MESSAGE, 0);
        countMap.put(MessageHolderType.ROOM_GROUP_ATTACHMENTS_MESSAGE, 0);
        countMap.put(MessageHolderType.SELF_GROUP_ATTACHMENTS_MESSAGE, 0);
    }

    @Override
    public BaseMessageViewHolder createViewHolder(int intType, ViewGroup parent,
                                                  MessageAdapterContract messageAdapterContract,
                                                  ViewHolderManagerContract viewHolderManagerContract)
            throws UnknownViewTypeException {
        MessageHolderType type = MessageHolderType.values()[intType];
        notifyCounter(type);

        boolean isSelfMessage = type.isSelf();
        switch (type) {
            case ROOM_TEXT_MESSAGE:
            case SELF_TEXT_MESSAGE:
                return new TextMessageViewHolder(parent, messageAdapterContract,
                        viewHolderManagerContract, isSelfMessage);
            case ROOM_BUBBLE_MESSAGE:
            case SELF_BUBBLE_MESSAGE:
                return new BubbleViewHolder(parent, messageAdapterContract,
                        viewHolderManagerContract, isSelfMessage);
            case ROOM_VOICE_MESSAGE:
            case SELF_VOICE_MESSAGE:
                return new VoiceViewHolder(parent, messageAdapterContract,
                        viewHolderManagerContract, isSelfMessage);
            case ROOM_GROUP_ATTACHMENTS_MESSAGE:
            case SELF_GROUP_ATTACHMENTS_MESSAGE:
                return new AttachmentGroupViewHolder(parent, messageAdapterContract,
                        viewHolderManagerContract, isSelfMessage);
            case ROOM_SINGLE_ATTACHMENT_MESSAGE:
            case SELF_SINGLE_ATTACHMENT_MESSAGE:
                return new MediaViewHolder(parent, messageAdapterContract,
                        viewHolderManagerContract, isSelfMessage);
            case ROOM_EMOJI_MESSAGE:
            case SELF_EMOJI_MESSAGE:
                return new EmojiMessageViewHolder(parent, messageAdapterContract,
                        viewHolderManagerContract, isSelfMessage);
            case ROOM_FILE_ATTACHMENT:
            case SELF_FILE_ATTACHMENT:
                return new FileAttachmentViewHolder(parent, messageAdapterContract,
                        viewHolderManagerContract, isSelfMessage);
            case ROOM_UPDATES:
                return new RoomUpdatesViewHolder(parent, messageAdapterContract,
                        viewHolderManagerContract);
        }
        throw new UnknownViewTypeException(intType);
    }

    @Override
    public MessageHolderType getViewHolderType(Message message, boolean isSelfMessage)
            throws UnknownViewTypeException {
        if (message.isDiraMessage()) {
            return MessageHolderType.ROOM_UPDATES;
        }

        if (isSelfMessage) {
            if (message.getAttachments().size() > 0) {
                if (message.getAttachments().size() != 1) {
                    return MessageHolderType.SELF_GROUP_ATTACHMENTS_MESSAGE;
                }
                Attachment attachment = message.getAttachments().get(0);
                if (attachment.getAttachmentType() == AttachmentType.VOICE) {
                    return MessageHolderType.SELF_VOICE_MESSAGE;
                } else if (attachment.getAttachmentType() == AttachmentType.BUBBLE) {
                    return MessageHolderType.SELF_BUBBLE_MESSAGE;
                } else if (attachment.getAttachmentType() == AttachmentType.FILE) {
                    return MessageHolderType.SELF_FILE_ATTACHMENT;
                }
                return MessageHolderType.SELF_SINGLE_ATTACHMENT_MESSAGE;

            } else if (message.hasText()) {
                if (StringFormatter.isEmoji(message.getText()) &&
                        StringFormatter.getEmojiCount(message.getText()) < 4) {
                    return MessageHolderType.SELF_EMOJI_MESSAGE;
                }
                return MessageHolderType.SELF_TEXT_MESSAGE;
            }
        }

        if (message.getAttachments().size() > 0) {
            Attachment attachment = message.getAttachments().get(0);
            if (message.getAttachments().size() != 1) {
                return MessageHolderType.ROOM_GROUP_ATTACHMENTS_MESSAGE;
            }
            if (attachment.getAttachmentType() == AttachmentType.VOICE) {
                return MessageHolderType.ROOM_VOICE_MESSAGE;
            } else if (attachment.getAttachmentType() == AttachmentType.BUBBLE) {
                return MessageHolderType.ROOM_BUBBLE_MESSAGE;
            } else if (attachment.getAttachmentType() == AttachmentType.FILE) {
                return MessageHolderType.ROOM_FILE_ATTACHMENT;
            }
            return MessageHolderType.ROOM_SINGLE_ATTACHMENT_MESSAGE;
        } else if (message.hasText()) {
            if (StringFormatter.isEmoji(message.getText()) &&
                    StringFormatter.getEmojiCount(message.getText()) < 4) {
                return MessageHolderType.ROOM_EMOJI_MESSAGE;
            }
            return MessageHolderType.ROOM_TEXT_MESSAGE;
        }
        message.setText("Unknown message type");
        return MessageHolderType.ROOM_TEXT_MESSAGE;
    }

    private void notifyCounter(MessageHolderType type) {
        if (!BuildConfig.DEBUG) return;

        if (!countMap.containsKey(type)) return;

        Integer count = countMap.get(type);
        count++;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            countMap.replace(type, count);
        }

        StringBuilder string = new StringBuilder();
        for (MessageHolderType key : countMap.keySet()) {
            string.append(key.name()).append(" - ").
                    append(countMap.get(key)).append("; ");
        }

        Logger.logDebug("RoomViewHolderFactory", string.toString());
    }
}
