package com.diraapp.ui.fragments.roominfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.DiraApplication;
import com.diraapp.R;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.adapters.roominfo.BaseAttachmentViewHolder;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class BaseRoomInfoFragment<Holder extends RecyclerView.ViewHolder,
        ConvertedType> extends Fragment
        implements AttachmentAdaptersListener, AttachmentLoader.AttachmentLoaderListener,
        BaseAttachmentViewHolder.FragmentViewHolderContract {

    private AttachmentLoader<ConvertedType> attachmentLoader;

    private List<AttachmentMessagePair> pairs;

    private RecyclerView.Adapter<Holder> adapter;

    protected final HashMap<String, Member> members;

    protected final Room room;
    private String selfId;

    private DiraActivity diraActivity;

    private RoomInfoFragmentListener listener;

    private RecyclerView recycler;

    private View noMediaView;

    public BaseRoomInfoFragment(int layoutId, HashMap<String, Member> members, Room room) {
        super(layoutId);
        this.members = members;
        this.room = room;
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
                              RecyclerView recycler, View noMediaView) {
        this.attachmentLoader = attachmentLoader;
        this.adapter = adapter;
        this.pairs = pairs;
        this.recycler = recycler;
        this.noMediaView = noMediaView;

        selfId = new CacheUtils(getContext()).getString(CacheUtils.ID);
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
        adapter.notifyItemRangeInserted(from, count);
    }

    @Override
    public void notifyItemsRemoved(int from, int count) {
        adapter.notifyItemRangeRemoved(from, count);
    }

    @Override
    public void notifyDataSetChanged() {
        Logger.logDebug(BaseRoomInfoFragment.class.getSimpleName(), "Recycler changed data set");

        adapter.notifyDataSetChanged();

        hideNoMedia();
    }

    @Override
    public void callScrollToMessage(String messageId, long messageTime) {
        if (listener == null) return;
        listener.scrollToMessage(messageId, messageTime);
    }

    private void hideNoMedia() {
        recycler.setVisibility(View.VISIBLE);
        noMediaView.setVisibility(View.GONE);
    }

    @Override
    public String getMemberName(String memberID) {
        if (selfId.equals(memberID)) {
            return getContext().getString(R.string.you);
        }

        Member member = members.get(memberID);

        if (member == null) {
            return getContext().getString(R.string.unknown);
        }

        return member.getNickname();
    }

    public void onNewMessage(Message message) {
        if (attachmentLoader == null) return;
        if (!attachmentLoader.isNewestLoaded()) return;
        Logger.logDebug(BaseRoomInfoFragment.class.getSimpleName(), "New update: isNewestLoaded = " + attachmentLoader.isNewestLoaded());

        final ArrayList<AttachmentMessagePair> newPairs = new ArrayList<>(message.getAttachments().size());

        for (Attachment attachment: message.getAttachments()) {
            AttachmentMessagePair thisPair = new AttachmentMessagePair();
            thisPair.setMessage(message);
            thisPair.setAttachment(attachment);

            newPairs.add(thisPair);
            Logger.logDebug(BaseRoomInfoFragment.class.getSimpleName(), "New update: attachment added");
        }

        diraActivity.runOnUiThread(() -> {
            attachmentLoader.insertNewPairs(newPairs);

            Logger.logDebug(BaseRoomInfoFragment.class.getSimpleName(), "New update: count = " + newPairs.size());
            if (newPairs.size() == 0) return;

            notifyItemsInserted(0, newPairs.size());
            scrollToLast();
            hideNoMedia();
        });
    }

    private void scrollToLast() {
        int lastVisiblePos = ((LinearLayoutManager)
                recycler.getLayoutManager()).findFirstVisibleItemPosition();

        if (lastVisiblePos < 3) {
            recycler.scrollToPosition(0);
        }
    }

    public interface RoomInfoFragmentListener {
        void scrollToMessage(String messageId, long messageTime);
    }

}
