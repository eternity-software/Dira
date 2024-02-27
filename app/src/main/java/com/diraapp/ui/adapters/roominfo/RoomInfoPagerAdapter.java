package com.diraapp.ui.adapters.roominfo;

import static com.diraapp.ui.activities.RoomInfoActivity.ROOM_SECRET_EXTRA;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.ui.fragments.roominfo.documents.FileRoomInfoFragment;
import com.diraapp.ui.fragments.roominfo.media.MediaRoomInfoFragment;
import com.diraapp.ui.fragments.roominfo.voice.VoiceRoomInfoFragment;
import com.diraapp.utils.Logger;

import java.util.HashMap;
import java.util.List;

public class RoomInfoPagerAdapter extends FragmentStateAdapter
        implements UpdateListener {

    private final HashMap<String, Member> members;

    private final Room room;

    private MediaRoomInfoFragment mediaFragment;
    private VoiceRoomInfoFragment voiceFragment;
    private FileRoomInfoFragment fileFragment;

    public RoomInfoPagerAdapter(@NonNull FragmentManager fragmentManager,
                                @NonNull Lifecycle lifecycle,
                                Room room, List<Member> memberList) {
        super(fragmentManager, lifecycle);
        this.room = room;

        members = new HashMap<>(memberList.size());
        for (Member member: memberList) {
            members.put(member.getId(), member);
        }

        UpdateProcessor.getInstance().addUpdateListener(this);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle = new Bundle();
        bundle.putString(ROOM_SECRET_EXTRA, room.getSecretName());

        Fragment fragment;

        switch (position) {
            case 1:
                voiceFragment = new VoiceRoomInfoFragment(members, room);
                fragment = voiceFragment;
                break;
            case 2:
                fileFragment = new FileRoomInfoFragment(members, room);
                fragment = fileFragment;
                break;

            default:
                mediaFragment = new MediaRoomInfoFragment(members, room);
                fragment = mediaFragment;
                break;
        }

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public void release() {
        UpdateProcessor.getInstance().removeUpdateListener(this);

        mediaFragment = null;
        voiceFragment = null;
        fileFragment = null;
    }

    @Override
    public void onUpdate(Update update) {
        if (!(update instanceof NewMessageUpdate)) return;
        Logger.logDebug(RoomInfoPagerAdapter.class.getSimpleName(), "New update");

        Message message = ((NewMessageUpdate) update).getMessage();

        if (message == null) return;
        if (message.getAttachments() == null) return;
        if (message.getAttachments().size() == 0) return;

        AttachmentType type = message.getSingleAttachment().getAttachmentType();
        Logger.logDebug(RoomInfoPagerAdapter.class.getSimpleName(), "New update: type = " + type);
        switch (type) {
            case IMAGE:
            case VIDEO:
                if (mediaFragment == null) return;
                mediaFragment.onNewMessage(message);
                break;
            case VOICE:
            case BUBBLE:
                if (voiceFragment == null) return;
                voiceFragment.onNewMessage(message);
                break;
            case FILE:
                if (fileFragment == null) return;
                fileFragment.onNewMessage(message);
                break;
        }

    }
}
