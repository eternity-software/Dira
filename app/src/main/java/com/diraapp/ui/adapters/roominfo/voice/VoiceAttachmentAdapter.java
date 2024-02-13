package com.diraapp.ui.adapters.roominfo.voice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.fragments.roominfo.voice.VoiceFragmentAdapterContract;

import java.util.List;

public class VoiceAttachmentAdapter extends RecyclerView.Adapter<VoiceAttachmentViewHolder> {

    private final Context context;

    private final LayoutInflater inflater;

    private final VoiceFragmentAdapterContract.ViewBindListener listener;

    private final VoiceFragmentAdapterContract.ViewClickListener viewClickListener;

    private final List<AttachmentMessagePair> pairs;

    public VoiceAttachmentAdapter(Context context, List<AttachmentMessagePair> pairs,
                                  VoiceFragmentAdapterContract.ViewBindListener listener,
                                  VoiceFragmentAdapterContract.ViewClickListener viewClickListener) {
        this.context = context;
        this.pairs = pairs;
        this.listener = listener;
        this.viewClickListener = viewClickListener;

        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public VoiceAttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VoiceAttachmentViewHolder(
                inflater.inflate(R.layout.listenable_attachment_element, parent, false),
                viewClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull VoiceAttachmentViewHolder holder, int position) {
        AttachmentMessagePair pair = pairs.get(position);

        holder.bind(pair);

        listener.onAttached(holder);
    }

    @Override
    public int getItemCount() {
        return 0;
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
