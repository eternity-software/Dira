package com.diraapp.ui.activities.room;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.ui.bottomsheet.filepicker.SelectorFileInfo;

import java.util.ArrayList;
import java.util.List;

public class MultiAttachmentLoader implements RoomActivityPresenter.AttachmentReadyListener {

    private String messageText;
    private List<Attachment> attachments= new ArrayList<>();

    private RoomActivityContract.Presenter presenter;

    private int readySize = -1;

    public MultiAttachmentLoader(String messageText, RoomActivityContract.Presenter roomActivityPresenter) {
        this.messageText = messageText;
        this.presenter = roomActivityPresenter;
    }

    public void send(List<SelectorFileInfo> selectorFileInfoList)
    {
        readySize = selectorFileInfoList.size();
        for(SelectorFileInfo selectorFileInfo : selectorFileInfoList)
        {
            AttachmentType attachmentType = AttachmentType.VIDEO;
            if (!selectorFileInfo.isVideo()) {
                attachmentType = AttachmentType.IMAGE;
            }
            presenter.uploadAttachment(attachmentType, this,
                    selectorFileInfo.getFilePath());
        }
    }

    @Override
    public void onReady(Attachment attachment) {
         attachments.add(attachment);
         if(attachments.size() == readySize)
         {
             presenter.sendMessage((ArrayList<Attachment>) attachments, messageText);
         }
    }


}
