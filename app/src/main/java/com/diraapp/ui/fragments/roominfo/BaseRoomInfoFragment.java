package com.diraapp.ui.fragments.roominfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.adapters.roominfo.BaseAttachmentViewHolder;
import com.diraapp.utils.Logger;

import java.util.List;

public abstract class BaseRoomInfoFragment<Holder extends RecyclerView.ViewHolder,
        ConvertedType> extends Fragment
        implements AttachmentAdaptersListener, AttachmentLoader.AttachmentLoaderListener,
            BaseAttachmentViewHolder.ScrollToMessageButtonListener {

    private AttachmentLoader<ConvertedType> attachmentLoader;

    private List<AttachmentMessagePair> pairs;

    private RecyclerView.Adapter<Holder> adapter;

    private DiraActivity diraActivity;

    private RoomInfoFragmentListener listener;

    private View recycler;

    private View noMediaView;

    public BaseRoomInfoFragment(int layoutId) {
        super(layoutId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        diraActivity = (DiraActivity) getActivity();
        listener = (RoomInfoFragmentListener) getActivity();

        return null;
    }

    protected void release() {
        attachmentLoader = null;
        adapter = null;
        diraActivity = null;
    }

    public void setupFragment(AttachmentLoader<ConvertedType> attachmentLoader,
                              RecyclerView.Adapter<Holder> adapter,
                              List<AttachmentMessagePair> pairs,
                              View recycler, View noMediaView) {
        this.attachmentLoader = attachmentLoader;
        this.adapter = adapter;
        this.pairs = pairs;
        this.recycler = recycler;
        this.noMediaView = noMediaView;
    }

    @Override
    public void onTopScrolled() {
        diraActivity.runBackground(() -> {
            attachmentLoader.loadNewerAttachments(pairs.get(0).getAttachment().getId());
        });
    }

    @Override
    public void onBottomScrolled() {
        diraActivity.runBackground(() -> {
            attachmentLoader.loadOlderAttachments(
                    pairs.get(pairs.size() - 1).getAttachment().getId());
        });
    }

    public void loadLatest() {
        diraActivity.runBackground(() -> {
            attachmentLoader.loadLatestAttachments();
        });
    }

    @Override
    public void notifyItemsInserted(int from, int count) {
        adapter.notifyItemRangeChanged(from, count);
    }

    @Override
    public void notifyItemsRemoved(int from, int count) {
        adapter.notifyItemRangeRemoved(from, count);
    }

    @Override
    public void notifyDataSetChanged() {
        Logger.logDebug(BaseRoomInfoFragment.class.getSimpleName(), "Recycler changed data set");

        adapter.notifyDataSetChanged();

        recycler.setVisibility(View.VISIBLE);
        noMediaView.setVisibility(View.GONE);
    }

    @Override
    public void callScrollToMessage(String messageId, long messageTime) {
        if (listener == null) return;
        listener.scrollToMessage(messageId, messageTime);
    }

    public interface RoomInfoFragmentListener {
        void scrollToMessage(String messageId, long messageTime);
    }

}
