package com.diraapp.ui.adapters.messages.views;

import com.diraapp.media.DiraMediaPlayer;
import com.diraapp.storage.MessageAttachmentLoader;

import java.util.concurrent.Executor;

import linc.com.amplituda.Amplituda;

public interface ViewHolderManagerContract {

    Amplituda getAmplituda();

    DiraMediaPlayer getDiraMediaPlayer();

    Executor getVoiceMessageThread();

    MessageAttachmentLoader getMessageAttachmentLoader();
}
