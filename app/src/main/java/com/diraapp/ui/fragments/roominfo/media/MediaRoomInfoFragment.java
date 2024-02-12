package com.diraapp.ui.fragments.roominfo.media;

import static com.diraapp.ui.activities.RoomInfoActivity.ROOM_SECRET_EXTRA;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.diraapp.R;
import com.diraapp.databinding.FragmentMediaRoominfoBinding;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.daos.AttachmentDao;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.storage.DiraMediaInfo;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.PreviewActivity;
import com.diraapp.ui.adapters.MediaGridItemListener;
import com.diraapp.ui.adapters.roominfo.MediaGridAdapter;
import com.diraapp.ui.bottomsheet.filepicker.SelectorFileInfo;
import com.diraapp.ui.fragments.roominfo.AttachmentLoader;
import com.diraapp.ui.fragments.roominfo.BaseRoomInfoFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaRoomInfoFragment extends BaseRoomInfoFragment<MediaGridAdapter.ViewHolder, SelectorFileInfo, Attachment> {

    private FragmentMediaRoominfoBinding binding;

    private List<SelectorFileInfo> list = new ArrayList<>();

    private String roomSecret;

    public MediaRoomInfoFragment() {
        super(R.layout.fragment_media_roominfo);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentMediaRoominfoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        AttachmentType[] types = new AttachmentType[2];
        types[0] = AttachmentType.IMAGE;
        types[1] = AttachmentType.VIDEO;

        roomSecret = requireArguments().getString(ROOM_SECRET_EXTRA);

        AttachmentLoader<SelectorFileInfo, Attachment> loader = getLoader(types);

        MediaGridItemListener itemListener = new MediaGridItemListener() {
            @Override
            public void onItemClick(int pos, View view) {
                DiraMediaInfo diraMediaInfo = list.get(pos);
                Intent intent = new Intent(getContext(), PreviewActivity.class);
                intent.putExtra(PreviewActivity.URI, diraMediaInfo.getFilePath());
                intent.putExtra(PreviewActivity.IS_VIDEO, diraMediaInfo.isVideo());
                startActivity(intent);
            }

            @Override
            public void onLastItemLoaded(int pos, View view) {

            }
        };

        MediaGridAdapter adapter = new MediaGridAdapter((DiraActivity) getActivity(), list,
                itemListener, binding.gridView, this);

        super.setAdapter(adapter);
        super.setAttachmentList(list);
        super.setAttachmentLoader(loader);
        super.setRecycler(binding.gridView);
        super.setNoMediaView(binding.noMediaIndicator);

        binding.gridView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        binding.gridView.setAdapter(adapter);

        loadLatest();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
        release();
    }

    private AttachmentLoader<SelectorFileInfo, Attachment> getLoader(AttachmentType[] types) {
        AttachmentDao attachmentDao = DiraMessageDatabase.getDatabase(getContext()).getAttachmentDao();
        return new AttachmentLoader<>(
                getContext(), list, this,
                new AttachmentLoader.AttachmentDataHelper<SelectorFileInfo, Attachment>() {
                    @Override
                    public SelectorFileInfo convert(Attachment attachment) {
                        File file = AttachmentDownloader.getFileFromAttachment(attachment, getContext(), roomSecret);

                        String mimeType = "image";

                        if (attachment.getAttachmentType() == AttachmentType.VIDEO) {
                            mimeType = "video";
                        }

                        if (file != null) {
                            return new SelectorFileInfo(file.getName(), file.getPath(), mimeType,
                                    attachment.getId(), attachment.getMessageId());
                        }
                        return null;
                    }

                    @Override
                    public List<Attachment> getLatest() {
                        return attachmentDao.getLatestAttachments(roomSecret, types);
                    }

                    @Override
                    public List<Attachment> getNewer(long id) {
                        return attachmentDao.getNewerAttachments(roomSecret, id, types);
                    }

                    @Override
                    public List<Attachment> getOlder(long id) {
                        return attachmentDao.getOlderAttachments(roomSecret, id, types);
                    }
                });
    }
}
