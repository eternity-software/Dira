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
import com.diraapp.ui.fragments.roominfo.media.MediaRoomInfoFragment;
import com.diraapp.ui.fragments.roominfo.voice.VoiceRoomInfoFragment;

import java.util.HashMap;
import java.util.List;

public class RoomInfoPagerAdapter extends FragmentStateAdapter {

    private String roomSecret;

    private HashMap<String, Member> members;

    public RoomInfoPagerAdapter(@NonNull FragmentManager fragmentManager,
                                @NonNull Lifecycle lifecycle,
                                String roomSecret, List<Member> memberList) {
        super(fragmentManager, lifecycle);
        this.roomSecret = roomSecret;

        members = new HashMap<>(memberList.size());
        for (Member member: memberList) {
            members.put(member.getId(), member);
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle = new Bundle();
        bundle.putString(ROOM_SECRET_EXTRA, roomSecret);

        Fragment fragment;

        switch (position) {
            case 1:
                fragment = new VoiceRoomInfoFragment(members);
                break;

            default:
                fragment = new MediaRoomInfoFragment(members);
        }

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
