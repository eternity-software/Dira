package com.diraapp.ui.activities;

import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;

import com.diraapp.databinding.ActivityMediaPreviewBinding;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.ui.adapters.mediapreview.MediaPageListener;
import com.diraapp.ui.adapters.mediapreview.MediaPreviewAdapter;
import com.diraapp.ui.adapters.mediapreview.MediaPreviewViewHolder;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.fragments.roominfo.AttachmentLoader;

import java.util.ArrayList;
import java.util.List;

public class MediaPreviewActivity extends DiraActivity
        implements MediaPageListener, AttachmentLoader.AttachmentLoaderListener,
            MediaPreviewViewHolder.ViewHolderActivityContract {

    public static String ROOM_SECRET = "ROOM_SECRET";
    public static String START_ATTACHMENT_ID = "START_ATTACHMENT_ID";

    private ActivityMediaPreviewBinding binding;

    private String roomSecret;

    private long startId;

    private final List<AttachmentMessagePair> pairs = new ArrayList<>();

    private AttachmentLoader<AttachmentMessagePair> loader;

    private MediaPreviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roomSecret = getIntent().getExtras().getString(ROOM_SECRET);
        startId = getIntent().getExtras().getLong(START_ATTACHMENT_ID);

        // setup adapter
        adapter = new MediaPreviewAdapter(this, pairs, this, this);
        binding.viewPager.setAdapter(adapter);

        // setup loader
        DiraActivity.runGlobalBackground(() -> {
            AttachmentType[] types = new AttachmentType[2];
            types[0] = AttachmentType.IMAGE;
            types[1] = AttachmentType.VIDEO;

            loader = new AttachmentLoader<>(this, pairs, roomSecret, types, this, 10);
            loader.loadNear(startId);
        });

        getWindow().getSharedElementEnterTransition().setInterpolator(new DecelerateInterpolator(2f));
        getWindow().getSharedElementEnterTransition().setDuration(250);
    }

    @Override
    public void onOldestPageOpened() {
        DiraActivity.runGlobalBackground(() -> {
            loader.loadOlderAttachments(pairs.get(pairs.size() - 1).getAttachment().getId());
        });

    }

    @Override
    public void onNewestPageOpened() {
        DiraActivity.runGlobalBackground(() -> {
            loader.loadNewerAttachments(pairs.get(0).getAttachment().getId());
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

        int pos = 0;
        for (int i = 0; i < pairs.size(); i++) {
            if (pairs.get(i).getAttachment().getId() == startId) {
                pos = i;
                break;
            }
        }

        binding.viewPager.setCurrentItem(pos);
    }

    @Override
    public void onWatchClicked() {

    }

    @Override
    public void attachVideoPlayer(DiraVideoPlayer videoPlayer) {
        videoPlayer.attachDiraActivity(this);
    }
}