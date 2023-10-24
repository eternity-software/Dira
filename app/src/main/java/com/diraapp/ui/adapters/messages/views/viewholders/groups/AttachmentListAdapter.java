package com.diraapp.ui.adapters.messages.views.viewholders.groups;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.diraapp.db.entities.Attachment;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.ui.components.ImagePreview;
import com.diraapp.utils.android.EmptyAttributeSet;

import java.util.List;

public class AttachmentListAdapter  extends ArrayAdapter<AttachmentItem> {
    private Context mContext;

    public AttachmentListAdapter(Context context,List<AttachmentItem> items) {
        super(context,0, items);
        this.mContext = context;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            convertView = new ImagePreview(mContext, EmptyAttributeSet.get(mContext.getResources()));
            AttachmentItem attachmentItem = getItem(position);
            ((ImagePreview) convertView).setAttachment(attachmentItem.getAttachment(),
                    attachmentItem.getRoom(),
                    AttachmentsStorage.getFileFromAttachment(attachmentItem.getAttachment(),
                            mContext,
                            attachmentItem.getRoom().getSecretName()), null);
        }
        return convertView;
    }
}
