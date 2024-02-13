package com.diraapp.ui.fragments.roominfo.voice;

import static com.diraapp.ui.activities.RoomInfoActivity.ROOM_SECRET_EXTRA;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.diraapp.R;
import com.diraapp.databinding.FragmentMediaRoominfoBinding;
import com.diraapp.databinding.FragmentVoiceRoominfoBinding;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.roominfo.voice.VoiceAttachmentAdapter;
import com.diraapp.ui.adapters.roominfo.voice.VoiceAttachmentViewHolder;
import com.diraapp.ui.bottomsheet.filepicker.SelectorFileInfo;
import com.diraapp.ui.fragments.roominfo.AttachmentLoader;
import com.diraapp.ui.fragments.roominfo.BaseRoomInfoFragment;
import com.diraapp.ui.singlemediaplayer.GlobalMediaPlayer;
import com.diraapp.ui.singlemediaplayer.GlobalMediaPlayerListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VoiceRoomInfoFragment extends
        BaseRoomInfoFragment<VoiceAttachmentViewHolder, AttachmentMessagePair>
        implements VoiceFragmentAdapterContract.ViewBindListener,
                    VoiceFragmentAdapterContract.ViewClickListener,
                    GlobalMediaPlayerListener {

    private final List<AttachmentMessagePair> pairs = new ArrayList<>();
    private FragmentVoiceRoominfoBinding binding;
    private String roomSecret;

    private VoiceAttachmentViewHolder currentViewHolder;

    public VoiceRoomInfoFragment() {
        super(R.layout.fragment_voice_roominfo);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentVoiceRoominfoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        roomSecret = requireArguments().getString(ROOM_SECRET_EXTRA);

        AttachmentLoader<AttachmentMessagePair> loader = createLoader();

        VoiceAttachmentAdapter adapter = new VoiceAttachmentAdapter(
                getContext(), pairs, this, this);

        super.setupFragment(loader, adapter, pairs, binding.recycler, binding.noVoiceIndicator);

        binding.recycler.setAdapter(adapter);

        loadLatest();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
        release();
    }

    private AttachmentLoader<AttachmentMessagePair> createLoader() {
        AttachmentType[] types = new AttachmentType[2];
        types[0] = AttachmentType.VOICE;
        types[1] = AttachmentType.BUBBLE;

        return new AttachmentLoader<>(getContext(), pairs, roomSecret, types, this);
    }

    @Override
    public void onAttached(VoiceAttachmentViewHolder holder) {
        if (holder.getPair() == null) return;

        if (!holder.getPair().getMessage().getId().equals(
                GlobalMediaPlayer.getInstance().getCurrentMessage().getId())) return;

        if (currentViewHolder != null) {
            currentViewHolder.close();
        }

        currentViewHolder = holder;
        currentViewHolder.onResume(GlobalMediaPlayer.getInstance().isPaused());
    }

    @Override
    public void onDetached(VoiceAttachmentViewHolder holder) {
        onRecycled(holder);
    }

    @Override
    public void onRecycled(VoiceAttachmentViewHolder holder) {
        if (currentViewHolder == null) return;

        if (holder.getPair() == null) return;

        if (!holder.getPair().getMessage().getId().equals(
                GlobalMediaPlayer.getInstance().getCurrentMessage().getId())) return;

        holder.close();
        currentViewHolder = null;
    }

    @Override
    public void onViewStartClicked(VoiceAttachmentViewHolder holder) {
        if (currentViewHolder != null) {
            currentViewHolder.close();
        }

        currentViewHolder = holder;

        AttachmentMessagePair pair = holder.getPair();

        String fileURL = pair.getAttachment().getFileUrl();

        if (fileURL == null) return;

        File file = new File(fileURL);

        if (!file.exists()) return;

        GlobalMediaPlayer.getInstance().changePlyingMessage(pair.getMessage(), file, 0);
    }

    @Override
    public void onCurrentViewClicked() {
        GlobalMediaPlayer.getInstance().onPaused();
    }

    @Override
    public void onGlobalMediaPlayerPauseClicked(boolean isPaused, float progress) {
        if (currentViewHolder == null) return;

        currentViewHolder.pause(isPaused);
    }

    @Override
    public void onGlobalMediaPlayerClose() {
        if (currentViewHolder == null) return;

        currentViewHolder.close();
    }

    @Override
    public void onGlobalMediaPlayerStart(Message message, File file) {
        if (currentViewHolder == null) return;

        currentViewHolder.start();
    }

    @Override
    public void onGlobalMediaPlayerProgressChanged(float progress, Message message) {
        // pass
    }
}
