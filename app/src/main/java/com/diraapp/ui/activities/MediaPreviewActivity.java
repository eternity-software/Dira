package com.diraapp.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.diraapp.databinding.ActivityMediaPreviewBinding;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.ui.adapters.mediapreview.MediaPageListener;
import com.diraapp.ui.adapters.mediapreview.MediaPreviewAdapter;
import com.diraapp.ui.adapters.mediapreview.MediaPreviewViewHolder;
import com.diraapp.ui.fragments.roominfo.AttachmentLoader;

import java.util.ArrayList;
import java.util.List;

public class MediaPreviewActivity extends AppCompatActivity
        implements MediaPageListener, AttachmentLoader.AttachmentLoaderListener {

    private static String ROOM_SECRET = "ROOM_SECRET";
    private static String START_ATTACHMENT_ID = "START_ATTACHMENT_ID";

    private ActivityMediaPreviewBinding binding;

    private String roomSecret;

    private final List<AttachmentMessagePair> pairs = new ArrayList<>();

    private AttachmentLoader<AttachmentMessagePair> loader;

    private MediaPreviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roomSecret = getIntent().getExtras().getString(ROOM_SECRET);
        long startId = getIntent().getExtras().getLong(START_ATTACHMENT_ID);

        // setup adapter
        MediaPreviewViewHolder.WatchCallBack watchCallBack = () -> {
            // TODO: write callback
        };
        adapter = new MediaPreviewAdapter(this, pairs, watchCallBack, this);

        // setup loader
        AttachmentType[] types = new AttachmentType[2];
        types[0] = AttachmentType.IMAGE;
        types[1] = AttachmentType.VIDEO;

        loader = new AttachmentLoader<>(this, pairs, roomSecret, types, this, 10);
        loader.loadNear(startId);
    }

    @Override
    public void onOldestPageOpened() {
        DiraActivity.runGlobalBackground(() -> {
            loader.loadNewerAttachments(pairs.get(0).getAttachment().getId());
        });

    }

    @Override
    public void onNewestPageOpened() {
        DiraActivity.runGlobalBackground(() -> {
            loader.loadNewerAttachments(pairs.get(pairs.size() - 1).getAttachment().getId());
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
        adapter.notifyDataSetChanged();
    }
}