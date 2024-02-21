package com.diraapp.ui.adapters.roominfo.voice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.ui.adapters.roominfo.BaseAttachmentAdapter;
import com.diraapp.ui.adapters.roominfo.BaseAttachmentViewHolder;
import com.diraapp.ui.fragments.roominfo.AttachmentAdaptersListener;
import com.diraapp.ui.fragments.roominfo.voice.VoiceFragmentAdapterContract;

import java.util.List;

public class VoiceAttachmentAdapter extends BaseAttachmentAdapter<VoiceAttachmentViewHolder> {

    private final Context context;

    private final LayoutInflater inflater;

    private final VoiceFragmentAdapterContract.ViewBindListener listener;

    private final VoiceFragmentAdapterContract.ViewClickListener viewClickListener;

    public VoiceAttachmentAdapter(Context context, List<AttachmentMessagePair> pairs,
                                  VoiceFragmentAdapterContract.ViewBindListener viewBindListener,
                                  VoiceFragmentAdapterContract.ViewClickListener viewClickListener,
                                  AttachmentAdaptersListener adaptersListener,
                                  BaseAttachmentViewHolder.FragmentViewHolderContract
                                          scrollToMessageButtonListener) {
        super(adaptersListener, pairs, scrollToMessageButtonListener);
        this.context = context;
        this.listener = viewBindListener;
        this.viewClickListener = viewClickListener;

        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public VoiceAttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VoiceAttachmentViewHolder(
                inflater.inflate(R.layout.roominfo_listenable_attachment, parent, false),
                viewClickListener, scrollToMessageButtonListener);
    }

    @Override
    public void onBindViewHolder(@NonNull VoiceAttachmentViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        listener.onAttached(holder);
    }

    @Override
    public void onViewRecycled(@NonNull VoiceAttachmentViewHolder holder) {
        super.onViewRecycled(holder);
        listener.onRecycled(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VoiceAttachmentViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        listener.onAttached(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VoiceAttachmentViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        listener.onDetached(holder);
    }
}
