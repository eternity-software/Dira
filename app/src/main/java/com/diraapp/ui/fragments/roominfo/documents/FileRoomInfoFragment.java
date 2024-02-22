package com.diraapp.ui.fragments.roominfo.documents;

import static com.diraapp.ui.activities.RoomInfoActivity.ROOM_SECRET_EXTRA;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.diraapp.R;
import com.diraapp.databinding.FragmentFilesRoominfoBinding;
import com.diraapp.databinding.FragmentVoiceRoominfoBinding;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.ui.adapters.roominfo.documents.FileAdapterContract;
import com.diraapp.ui.adapters.roominfo.documents.FileAttachmentAdapter;
import com.diraapp.ui.adapters.roominfo.documents.FileAttachmentViewHolder;
import com.diraapp.ui.adapters.roominfo.voice.VoiceAttachmentAdapter;
import com.diraapp.ui.adapters.roominfo.voice.VoiceAttachmentViewHolder;
import com.diraapp.ui.fragments.roominfo.AttachmentLoader;
import com.diraapp.ui.fragments.roominfo.BaseRoomInfoFragment;
import com.diraapp.ui.singlemediaplayer.GlobalMediaPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileRoomInfoFragment extends
        BaseRoomInfoFragment<FileAttachmentViewHolder, AttachmentMessagePair> {

    private final List<AttachmentMessagePair> pairs = new ArrayList<>();
    private FragmentFilesRoominfoBinding binding;
    private String roomSecret;

    private FileAttachmentAdapter adapter;

    public FileRoomInfoFragment(HashMap<String, Member> members, Room room) {
        super(R.layout.fragment_files_roominfo, members, room);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentFilesRoominfoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        roomSecret = requireArguments().getString(ROOM_SECRET_EXTRA);

        AttachmentLoader<AttachmentMessagePair> loader = createLoader();

        adapter = new FileAttachmentAdapter(
                this, pairs, this, getContext(), room);

        super.setupFragment(loader, adapter, pairs, binding.recycler, binding.noVoiceIndicator);

        binding.recycler.setAdapter(adapter);

        loadLatest();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
        adapter.release();
        adapter = null;

        release();
    }

    private AttachmentLoader<AttachmentMessagePair> createLoader() {
        AttachmentType[] types = new AttachmentType[1];
        types[0] = AttachmentType.FILE;

        return new AttachmentLoader<>(getContext(), pairs, roomSecret, types, this);
    }
}
