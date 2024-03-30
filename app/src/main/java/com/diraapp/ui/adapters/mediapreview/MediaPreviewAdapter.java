package com.diraapp.ui.adapters.mediapreview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;

import java.util.ArrayList;
import java.util.List;

public class MediaPreviewAdapter extends RecyclerView.Adapter<MediaPreviewViewHolder> {


    private final List<AttachmentMessagePair> pairs;

    private final MediaPreviewViewHolder.WatchCallBack watchCallBack;

    private final MediaPageListener listener;

    private final LayoutInflater inflater;

    public MediaPreviewAdapter(Context context, List<AttachmentMessagePair> pairs,
                               MediaPreviewViewHolder.WatchCallBack watchCallBack,
                               MediaPageListener listener) {
        this.pairs = pairs;
        this.watchCallBack = watchCallBack;
        this.listener = listener;

        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MediaPreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MediaPreviewViewHolder(
                inflater.inflate(R.layout.media_preview_layout, parent, false),
                watchCallBack);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaPreviewViewHolder holder, int position) {

        if (position == 0) {
            listener.onOldestPageOpened();
        } else if (position == pairs.size() - 1) {
            listener.onNewestPageOpened();
        }

        AttachmentMessagePair currentPair = pairs.get(position);
        holder.bind(currentPair);
    }

    @Override
    public int getItemCount() {
        return pairs.size();
    }


}
