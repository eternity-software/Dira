package com.diraapp.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.diraapp.databinding.ActivityMediaPreviewBinding;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.ui.adapters.mediapreview.MediaPageListener;
import com.diraapp.ui.adapters.mediapreview.MediaPreviewAdapter;
import com.diraapp.ui.adapters.mediapreview.MediaPreviewViewHolder;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.fragments.roominfo.AttachmentLoader;
import com.diraapp.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class MediaPreviewActivity extends DiraActivity
        implements MediaPageListener, AttachmentLoader.AttachmentLoaderListener,
            MediaPreviewViewHolder.ViewHolderActivityContract {

    public static String ROOM_SECRET = "ROOM_SECRET";
    public static String START_ATTACHMENT_ID = "START_ATTACHMENT_ID";

    public static String START_ATTACHMENT_URL = "START_ATTACHMENT_PATH";

    private ActivityMediaPreviewBinding binding;

    private String roomSecret;

    private String startUrl;

    private final List<AttachmentMessagePair> pairs = new ArrayList<>();

    private AttachmentLoader<AttachmentMessagePair> loader;

    private MediaPreviewAdapter adapter;

    private AttachmentMessagePair currentPair = null;

    private int currentSelectedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roomSecret = getIntent().getExtras().getString(ROOM_SECRET);
        long startId = getIntent().getExtras().getLong(START_ATTACHMENT_ID);
        startUrl = getIntent().getExtras().getString(START_ATTACHMENT_URL);

        // setup adapter
        initViewPager();

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
        int pos = 0;
        for (int i = 0; i < pairs.size(); i++) {
            String url = pairs.get(i).getAttachment().getFileUrl();
            if (url == null) continue;

            if (url.equals(startUrl)) {
                pos = i;
                break;
            }
        }

        adapter = new MediaPreviewAdapter(this, pairs, this, this);

        binding.viewPager.setClipToPadding(false);
        binding.viewPager.setClipChildren(false);
        binding.viewPager.setOffscreenPageLimit(5);
        binding.viewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                MediaPreviewViewHolder holder = (MediaPreviewViewHolder) ((RecyclerView)
                        binding.viewPager.getChildAt(0)).findViewHolderForAdapterPosition(position);
                Logger.logDebug(MediaPreviewActivity.class.getSimpleName(), "Selected pos = " + position);
                if (holder != null) {
                    holder.onSelected();
                }

                MediaPreviewViewHolder previousHolder;
                if (currentSelectedId != -1) {
                    previousHolder = (MediaPreviewViewHolder) ((RecyclerView)
                            binding.viewPager.getChildAt(0)).findViewHolderForAdapterPosition(currentSelectedId);

                    if (previousHolder != null) {
                        previousHolder.onUnselected();
                    }
                }

                currentPair = pairs.get(position);
                currentSelectedId = position;
            }
        });
        binding.viewPager.setAdapter(adapter);

        binding.viewPager.setCurrentItem(pos, false);
    }

    private void initViewPager() {
        // setup loader
        DiraActivity.runGlobalBackground(() -> {
            AttachmentType[] types = new AttachmentType[2];
            types[0] = AttachmentType.IMAGE;
            types[1] = AttachmentType.VIDEO;

            loader = new AttachmentLoader<>(this, pairs, roomSecret, types, this, 10);
            loader.loadNear(startUrl);
        });
    }

    @Override
    public void onWatchClicked() {

    }

    @Override
    public void attachVideoPlayer(DiraVideoPlayer videoPlayer) {
        videoPlayer.attachDiraActivity(this);
    }

    @Override
    public boolean checkIsSelected(Attachment attachment) {
        if (currentPair == null) return false;

        return currentPair.getAttachment().equals(attachment);
    }
}