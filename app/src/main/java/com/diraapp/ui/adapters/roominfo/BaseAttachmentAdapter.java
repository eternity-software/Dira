package com.diraapp.ui.adapters.roominfo;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.ui.fragments.roominfo.AttachmentAdaptersListener;

import java.util.List;

public abstract class BaseAttachmentAdapter<Holder extends BaseAttachmentViewHolder>
        extends RecyclerView.Adapter<Holder> {

    protected final List<AttachmentMessagePair> pairs;
    protected final BaseAttachmentViewHolder.FragmentViewHolderContract scrollToMessageButtonListener;
    private final AttachmentAdaptersListener adaptersListener;

    public BaseAttachmentAdapter(AttachmentAdaptersListener adaptersListener,
                                 List<AttachmentMessagePair> pairs,
                                 BaseAttachmentViewHolder.FragmentViewHolderContract scrollToMessageButtonListener) {
        this.adaptersListener = adaptersListener;
        this.pairs = pairs;
        this.scrollToMessageButtonListener = scrollToMessageButtonListener;
    }

    private void notifyScrollListener(int pos) {
        if (adaptersListener == null) return;
        if (pos == 0) {
            adaptersListener.onTopScrolled();
        } else if (pos == pairs.size() - 1) {
            adaptersListener.onBottomScrolled();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        notifyScrollListener(position);

        AttachmentMessagePair pair = pairs.get(position);

        holder.bind(pair);
    }

    @Override
    public int getItemCount() {
        return pairs.size();
    }
}
