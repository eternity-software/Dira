package com.diraapp.ui.fragments.roominfo.voice;

import com.diraapp.ui.adapters.roominfo.voice.VoiceAttachmentViewHolder;

public interface VoiceFragmentAdapterContract {

    interface ViewBindListener {
        void onAttached(VoiceAttachmentViewHolder holder);

        void onDetached(VoiceAttachmentViewHolder holder);

        void onRecycled(VoiceAttachmentViewHolder holder);

    }

    interface ViewClickListener {
        void onViewStartClicked(VoiceAttachmentViewHolder holder);

        void onCurrentViewClicked();

    }
}
