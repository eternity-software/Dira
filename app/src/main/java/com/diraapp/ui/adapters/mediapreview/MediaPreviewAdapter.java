package com.diraapp.ui.adapters.mediapreview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;

import java.util.List;

public class MediaPreviewAdapter extends RecyclerView.Adapter<MediaPreviewViewHolder> {


    private final List<AttachmentMessagePair> pairs;

    private final MediaPreviewViewHolder.ViewHolderActivityContract watchCallBack;

    private final MediaPageListener listener;

    private final LayoutInflater inflater;

    public MediaPreviewAdapter(Context context, List<AttachmentMessagePair> pairs,
                               MediaPreviewViewHolder.ViewHolderActivityContract holderActivityContract,
                               MediaPageListener listener) {
        this.pairs = pairs;
        this.watchCallBack = holderActivityContract;
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
            listener.onNewestPageOpened();
        } else if (position == pairs.size() - 1) {
            listener.onOldestPageOpened();
        }

        AttachmentMessagePair currentPair = pairs.get(position);
        holder.bind(currentPair);
    }

    @Override
    public int getItemCount() {
        return pairs.size();
    }

    @Override
    public void onViewRecycled(@NonNull MediaPreviewViewHolder holder) {
        super.onViewRecycled(holder);
        holder.release();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull MediaPreviewViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onAttached();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MediaPreviewViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onDetached();
    }

}
