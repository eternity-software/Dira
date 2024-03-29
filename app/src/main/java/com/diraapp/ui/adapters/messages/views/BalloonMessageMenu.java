package com.diraapp.ui.adapters.messages.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.api.requests.PinnedMessageAddedRequest;
import com.diraapp.api.requests.PinnedMessageRemovedRequest;
import com.diraapp.api.requests.Request;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.MessageReading;
import com.diraapp.storage.AppStorage;
import com.diraapp.ui.adapters.messagetooltipread.MessageTooltipAdapter;
import com.diraapp.ui.adapters.messagetooltipread.UserReadMessage;
import com.diraapp.ui.components.DiraPopup;
import com.diraapp.utils.android.DeviceUtils;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.OnBalloonOutsideTouchListener;
import com.skydoves.balloon.radius.RadiusLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class BalloonMessageMenu {

    private static boolean isBalloonActive = false;
    private final BalloonMenuListener listener;
    private final Balloon balloon;
    private final HashMap<String, Member> members;
    private final Context context;
    private final String selfId;

    public BalloonMessageMenu(Context context, HashMap<String, Member> members,
                              String selfId, @NonNull BalloonMenuListener listener) {
        this.listener = listener;
        this.members = members;
        this.context = context;
        this.selfId = selfId;
        balloon = new Balloon.Builder(context).
                setLayout(R.layout.message_actions_tooltip)
                .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                .setIsVisibleArrow(false)
                .build();

    }

    public void createBalloon(Message message, View view) {
        if (isBalloonActive) return;
        isBalloonActive = true;

        if (message.getMessageReadingList() == null) return;
        if (!message.hasAuthor()) return;
        ArrayList<UserReadMessage> userReadMessages = new ArrayList<>(
                message.getMessageReadingList().size());

        for (MessageReading messageReading : message.getMessageReadingList()) {
            Member member = members.get(messageReading.getUserId());
            if (member == null) continue;

            boolean isListened = messageReading.isHasListened();

            UserReadMessage userReadMessage = new UserReadMessage(
                    member.getNickname(), member.getImagePath(), isListened);
            userReadMessages.add(userReadMessage);
        }


        RadiusLayout layout = (RadiusLayout) balloon.getContentView();
        layout.setFocusable(false);

        layout.setBackground(ContextCompat.getDrawable(context, R.drawable.tooltip_drawable));
        LinearLayout replyRow = layout.findViewById(R.id.reply_row);
        LinearLayout copyRow = layout.findViewById(R.id.copy_row);
        LinearLayout deleteRow = layout.findViewById(R.id.delete_row);
        LinearLayout countRow = layout.findViewById(R.id.count_row);

        int size = userReadMessages.size();
        CardView firstCard = countRow.findViewById(R.id.card_view_1);
        CardView secondCard = countRow.findViewById(R.id.card_view_2);
        ImageView firstUserIcon = countRow.findViewById(R.id.icon_user_1);
        ImageView secondUserIcon = countRow.findViewById(R.id.icon_user_2);
        TextView countTextView = countRow.findViewById(R.id.count_row_text);

        initDownloadButton(message, layout);

        boolean needCopyRow = message.hasText();
        if (!needCopyRow) copyRow.setVisibility(View.GONE);

        String countText = "";
        if (size == 0) {
            firstCard.setVisibility(View.GONE);
            secondCard.setVisibility(View.GONE);
            if (message.getAuthorId().equals(selfId)) {
                countText = context.getString(R.string.message_tooltip_zero_read);
            } else {
                countText = context.getString(R.string.message_tooltip_only_you_read);
            }
        } else if (size == 1) {
            if (userReadMessages.get(0).getPicturePath() != null) {
                Picasso.get().load(new File(userReadMessages.get(0).getPicturePath())).into(firstUserIcon);
            } else {
                firstUserIcon.setImageResource(R.drawable.placeholder);
            }
            secondCard.setVisibility(View.GONE);
            countText = context.getString(R.string.message_tooltip_one_read).
                    replace("%s", String.valueOf(size));
        } else {
            if (userReadMessages.get(0).getPicturePath() != null) {
                Picasso.get().load(new File(userReadMessages.get(0).getPicturePath())).into(firstUserIcon);
            } else {
                firstUserIcon.setImageResource(R.drawable.placeholder);
            }
            if (userReadMessages.get(1).getPicturePath() != null) {
                Picasso.get().load(new File(userReadMessages.get(1).getPicturePath())).into(secondUserIcon);
            } else {
                secondUserIcon.setImageResource(R.drawable.placeholder);
            }
            countText = context.getString(R.string.message_tooltip_read_count).
                    replace("%s", String.valueOf(size));
        }
        countTextView.setText(countText);

        // Create balloon with list of reads
        if (size > 0) {
            Balloon listBalloon = new Balloon.Builder(context).
                    setLayout(R.layout.message_actions_tooltip_list)
                    .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                    .setIsVisibleArrow(false)
                    .build();

            RadiusLayout listLayout = (RadiusLayout) listBalloon.getContentView();
            listLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.tooltip_drawable));

            RelativeLayout recyclerLayout = listLayout.findViewById(R.id.recycler_layout);
            RecyclerView recyclerView = listLayout.findViewById(R.id.message_tooltip_recycler);

            LinearLayout countListRow = listLayout.findViewById(R.id.count_row);

            TextView countListTextView = listLayout.findViewById(R.id.count_row_text);
            countListTextView.setText(countText);

            if (size > 4) {
                int height = 4 * 48;
                height = DeviceUtils.dpToPx(height, context);

                recyclerLayout.getLayoutParams().height = height;
                recyclerLayout.requestLayout();
            }

            MessageTooltipAdapter adapter = new MessageTooltipAdapter(
                    context, userReadMessages, message.isListenable());
            recyclerView.setAdapter(adapter);

            countListRow.setOnClickListener((View v) -> {
                listBalloon.dismiss();
                balloon.showAlignBottom(view);
                isBalloonActive = true;
            });

            countRow.setOnClickListener((View v) -> {
                balloon.dismiss();
                listBalloon.showAlignBottom(view);
                isBalloonActive = true;
            });

            listBalloon.setOnBalloonDismissListener(() -> {
                isBalloonActive = false;
            });

            listBalloon.setOnBalloonOutsideTouchListener((new OnBalloonOutsideTouchListener() {
                @Override
                public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                    listBalloon.dismiss();
                }
            }));
        }

        fillPinnedRow(message, layout);

        replyRow.setOnClickListener((View v) -> {
            listener.onNewMessageReply(message);
            balloon.dismiss();
        });

        deleteRow.setOnClickListener((View v) -> {
            createDeletionPopup(message);
        });

        copyRow.setOnClickListener((View v) -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Text", message.getText());
            clipboard.setPrimaryClip(clip);
            balloon.dismiss();
        });

        balloon.setOnBalloonOutsideTouchListener((new OnBalloonOutsideTouchListener() {
            @Override
            public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                balloon.dismiss();
            }
        }));

        balloon.setOnBalloonDismissListener(() -> {
            isBalloonActive = false;
        });

        listener.onBalloonShown();

        balloon.showAlignBottom(view);
    }

    private void initDownloadButton(Message message, RadiusLayout layout) {
        if (message.getAttachments().size() != 1) return;

        Attachment attachment = message.getSingleAttachment();

        if (attachment.getAttachmentType() != AttachmentType.FILE) return;

        LinearLayout downloadLayout = layout.findViewById(R.id.save_row);
        downloadLayout.setVisibility(View.VISIBLE);

        downloadLayout.setOnClickListener((View v) -> {

            AppStorage.saveFileToDownloads(attachment, context, message.getRoomSecret());
            balloon.dismiss();
        });

    }

    private void createDeletionPopup(Message message) {
        DiraPopup diraPopup = new DiraPopup(context);

        diraPopup.show(context.getString(R.string.message_tooltip_delete_title),
                context.getString(R.string.message_tooltip_delete_text),
                null, null, () -> {
                    listener.onMessageDelete(message);
                });
        balloon.dismiss();
    }

    private void fillPinnedRow(Message message, RadiusLayout layout) {
        LinearLayout pinRow = layout.findViewById(R.id.pin_row);

        if (listener.isMessagePinned(message.getId())) {
            TextView pinText = pinRow.findViewById(R.id.pin_row_text);
            ImageView pinImage = pinRow.findViewById(R.id.pin_row_image);

            pinText.setText(context.getString(R.string.message_tooltip_unpin));
            // change icon

            pinRow.setOnClickListener((View v) -> {
                PinnedMessageRemovedRequest request = new PinnedMessageRemovedRequest(
                        message.getRoomSecret(), message.getId(), selfId);

                listener.sendRequest(request);
                balloon.dismiss();
            });
        } else {
            pinRow.setOnClickListener((View v) -> {
                PinnedMessageAddedRequest request = new PinnedMessageAddedRequest(
                        message.getRoomSecret(), message.getId(), selfId);

                listener.sendRequest(request);
                balloon.dismiss();
            });
        }
    }

    public interface BalloonMenuListener {

        void onBalloonShown();

        void onNewMessageReply(Message message);

        void onMessageDelete(Message message);

        boolean isMessagePinned(String messageId);

        void sendRequest(Request request);
    }
}
