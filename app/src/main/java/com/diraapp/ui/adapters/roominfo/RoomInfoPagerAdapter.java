package com.diraapp.ui.adapters.roominfo;

import static com.diraapp.ui.activities.RoomInfoActivity.ROOM_SECRET_EXTRA;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.diraapp.R;
import com.diraapp.ui.fragments.roominfo.media.MediaRoomInfoFragment;
import com.diraapp.ui.fragments.roominfo.voice.VoiceRoomInfoFragment;

public class RoomInfoPagerAdapter extends FragmentStateAdapter {

    private String roomSecret;

    public RoomInfoPagerAdapter(@NonNull FragmentManager fragmentManager,
                                @NonNull Lifecycle lifecycle,
                                String roomSecret) {
        super(fragmentManager, lifecycle);
        this.roomSecret = roomSecret;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle = new Bundle();
        bundle.putString(ROOM_SECRET_EXTRA, roomSecret);

        Fragment fragment;

        switch (position) {
            case 1:
                fragment = new VoiceRoomInfoFragment();
                break;

            default:
                fragment = new MediaRoomInfoFragment();
        }

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
