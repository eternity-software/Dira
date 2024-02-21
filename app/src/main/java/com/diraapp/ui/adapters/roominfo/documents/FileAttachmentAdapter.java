package com.diraapp.ui.adapters.roominfo.documents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.ui.adapters.roominfo.BaseAttachmentAdapter;
import com.diraapp.ui.adapters.roominfo.BaseAttachmentViewHolder;
import com.diraapp.ui.fragments.roominfo.AttachmentAdaptersListener;

import java.util.List;

public class FileAttachmentAdapter extends BaseAttachmentAdapter<FileAttachmentViewHolder> {

    private final LayoutInflater inflater;

    private final String roomSecret;

    private final String serverAddress;


    public FileAttachmentAdapter(AttachmentAdaptersListener adaptersListener,
                                 List<AttachmentMessagePair> pairs,
                                 BaseAttachmentViewHolder.FragmentViewHolderContract scrollToMessageButtonListener,
                                 Context context,
                                 String roomSecret,
                                 String serverAddress) {
        super(adaptersListener, pairs, scrollToMessageButtonListener);
        this.inflater = LayoutInflater.from(context);
        this.roomSecret = roomSecret;
        this.serverAddress = serverAddress;
    }

    @NonNull
    @Override
    public FileAttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileAttachmentViewHolder(inflater.inflate
                (R.layout.roominfo_listenable_attachment, parent, false),
                scrollToMessageButtonListener, roomSecret, serverAddress);
    }

    @Override
    public void onBindViewHolder(@NonNull FileAttachmentViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }
}
