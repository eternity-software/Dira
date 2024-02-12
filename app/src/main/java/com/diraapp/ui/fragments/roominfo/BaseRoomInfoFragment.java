package com.diraapp.ui.fragments.roominfo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.storage.AttachmentInfo;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.utils.Logger;

import java.util.List;

public abstract class BaseRoomInfoFragment<Holder extends RecyclerView.ViewHolder,
                                            ConvertedType> extends Fragment
        implements ScrollPositionListener, AttachmentLoader.AttachmentLoaderListener {

    private AttachmentLoader<ConvertedType> attachmentLoader;

    private List<AttachmentMessagePair> pairs;

    private RecyclerView.Adapter<Holder> adapter;

    private DiraActivity diraActivity;

    private View recycler;

    private View noMediaView;

    public BaseRoomInfoFragment(int layoutId) {
        super(layoutId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        diraActivity = (DiraActivity) getActivity();

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
            boolean success = attachmentLoader.loadNewerAttachments(pairs.get(0).getAttachment().getId());

            Logger.logDebug(BaseRoomInfoFragment.class.getSimpleName(), "Top loaded with success = " + success);
        });
    }

    @Override
    public void onBottomScrolled() {
        diraActivity.runBackground(() -> {
            boolean success = attachmentLoader.loadOlderAttachments(
                    pairs.get(pairs.size() - 1).getAttachment().getId());

            Logger.logDebug(BaseRoomInfoFragment.class.getSimpleName(), "Bottom loaded with success = " + success);

        });
    }

    public void loadLatest() {
        diraActivity.runBackground(() -> {
            boolean success = attachmentLoader.loadLatestAttachments();

            setRecyclerVisibility(success);
        });
    }

    @Override
    public void notifyItemsInserted(int from, int count) {
        new Handler(Looper.getMainLooper()).post(() -> {
            adapter.notifyItemRangeChanged(from, count);
        });
    }

    @Override
    public void notifyItemsRemoved(int from, int count) {
        new Handler(Looper.getMainLooper()).post(() -> {
            adapter.notifyItemRangeRemoved(from, count);
        });
    }

    @Override
    public void notifyDataSetChanged() {
        diraActivity.runOnUiThread(() -> {
            Logger.logDebug(BaseRoomInfoFragment.class.getSimpleName(), "Recycler changed data set");

            adapter.notifyDataSetChanged();
        });
    }

    public void setRecyclerVisibility(boolean isVisible) {
        diraActivity.runOnUiThread(() -> {
            Logger.logDebug(BaseRoomInfoFragment.class.getSimpleName(), "Recycler visibility = " + isVisible);
            if (isVisible) {
                recycler.setVisibility(View.VISIBLE);
                noMediaView.setVisibility(View.GONE);
            } else {
                recycler.setVisibility(View.GONE);
                noMediaView.setVisibility(View.VISIBLE);
            }
        });
    }

}
