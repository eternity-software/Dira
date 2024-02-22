package com.diraapp.ui.adapters.roominfo.documents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.storage.MessageAttachmentLoader;
import com.diraapp.ui.adapters.roominfo.BaseAttachmentAdapter;
import com.diraapp.ui.adapters.roominfo.BaseAttachmentViewHolder;
import com.diraapp.ui.fragments.roominfo.AttachmentAdaptersListener;

import java.util.List;

public class FileAttachmentAdapter extends BaseAttachmentAdapter<FileAttachmentViewHolder>
        implements FileAdapterContract {

    private final LayoutInflater inflater;

    private final String roomSecret;

    private final String serverAddress;

    private final MessageAttachmentLoader loader;


    public FileAttachmentAdapter(AttachmentAdaptersListener adaptersListener,
                                 List<AttachmentMessagePair> pairs,
                                 BaseAttachmentViewHolder.FragmentViewHolderContract scrollToMessageButtonListener,
                                 Context context,
                                 Room room) {
        super(adaptersListener, pairs, scrollToMessageButtonListener);
        this.inflater = LayoutInflater.from(context);
        this.roomSecret = room.getSecretName();
        this.serverAddress = room.getServerAddress();

        loader = new MessageAttachmentLoader(room, context);
    }

    @NonNull
    @Override
    public FileAttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileAttachmentViewHolder(inflater.inflate
                (R.layout.roominfo_file_attachment, parent, false),
                scrollToMessageButtonListener, roomSecret, serverAddress, this);
    }

    @Override
    public void onBindViewHolder(@NonNull FileAttachmentViewHolder holder, int position) {

        loader.removeListener(holder.getAttachmentStorageListener());
        holder.removeAttachmentStorageListener();

        super.onBindViewHolder(holder, position);
    }

    public void release() {
        loader.release();
    }

    @Override
    public MessageAttachmentLoader getLoader() {
        return loader;
    }
}
