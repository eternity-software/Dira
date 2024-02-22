package com.diraapp.ui.adapters.roominfo;

import static com.diraapp.ui.activities.RoomInfoActivity.ROOM_SECRET_EXTRA;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.diraapp.R;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.ui.fragments.roominfo.documents.FileRoomInfoFragment;
import com.diraapp.ui.fragments.roominfo.media.MediaRoomInfoFragment;
import com.diraapp.ui.fragments.roominfo.voice.VoiceRoomInfoFragment;

import java.util.HashMap;
import java.util.List;

public class RoomInfoPagerAdapter extends FragmentStateAdapter {

    private final HashMap<String, Member> members;

    private final Room room;

    public RoomInfoPagerAdapter(@NonNull FragmentManager fragmentManager,
                                @NonNull Lifecycle lifecycle,
                                Room room, List<Member> memberList) {
        super(fragmentManager, lifecycle);
        this.room = room;

        members = new HashMap<>(memberList.size());
        for (Member member: memberList) {
            members.put(member.getId(), member);
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle = new Bundle();
        bundle.putString(ROOM_SECRET_EXTRA, room.getSecretName());

        Fragment fragment;

        switch (position) {
            case 1:
                fragment = new VoiceRoomInfoFragment(members, room);
                break;
            case 3:
                fragment = new FileRoomInfoFragment(members, room);
                break;

            default:
                fragment = new MediaRoomInfoFragment(members, room);
        }

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
