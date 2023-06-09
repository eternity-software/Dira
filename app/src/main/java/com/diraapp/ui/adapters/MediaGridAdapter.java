package com.diraapp.ui.adapters;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.ui.bottomsheet.filepicker.FileInfo;
import com.diraapp.ui.components.FilePreview;
import com.diraapp.storage.images.WaterfallBalancer;

import java.util.ArrayList;


public class MediaGridAdapter extends RecyclerView.Adapter<MediaGridAdapter.ViewHolder> {


    private final int threadCount = 0;
    private final LayoutInflater mInflater;
    private final ArrayList<FileInfo> mediaElements;
    private final WaterfallBalancer waterfallBalancer;
    private final Activity context;
    private final MediaGridItemListener itemClickListener;
    private Runnable transitionReenter;

    /**
     * Constructor for custom files arrays
     *
     * @param context
     * @param itemClickListener
     * @param recyclerView
     */
    public MediaGridAdapter(final Activity context, ArrayList<FileInfo> mediaElements, MediaGridItemListener itemClickListener, RecyclerView recyclerView) {
        this.mInflater = LayoutInflater.from(context);
        this.itemClickListener = itemClickListener;
        this.context = context;

        this.mediaElements = mediaElements;
        //   Collections.reverse(images);
        waterfallBalancer = new WaterfallBalancer(context, 5, recyclerView);


    }

    /**
     * Constructor to get whole device memory
     *
     * @param context
     * @param itemClickListener
     * @param recyclerView
     */
    public MediaGridAdapter(final Activity context, MediaGridItemListener itemClickListener, RecyclerView recyclerView) {
        this.mInflater = LayoutInflater.from(context);
        this.itemClickListener = itemClickListener;
        this.context = context;
        mediaElements = getAllShownImagesPath();
        //   Collections.reverse(images);
        waterfallBalancer = new WaterfallBalancer(context, 5, recyclerView);

        registerTransitionListener();
    }

    public void setBalancerCallback(WaterfallBalancer.BalancerCallback balancerCallback) {
        waterfallBalancer.setBalancerCallback(balancerCallback);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.file_picker_image, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.fileParingImageView.getFileParingImageView().setImageDrawable(null);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FilePreview picturesView = holder.fileParingImageView;
        picturesView.setFileInfo(mediaElements.get(position));
        System.out.println(mediaElements.get(position).getMimeType());

        try {

            picturesView.getFileParingImageView().setBackgroundColor(context.getResources().getColor(R.color.dark));
            picturesView.getFileParingImageView().setImageDrawable(null);

            waterfallBalancer.add(picturesView);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mediaElements.size();
    }

    public ArrayList<FileInfo> getMediaElements() {
        return mediaElements;
    }

    // Convenience method for getting data at click position
    public FileInfo getItem(int id) {
        return mediaElements.get(id);
    }

    // Method that executes your code for the action received
    public void onItemClick(final View view, int position) {
        Log.i("TAG", "You clicked number " + getItem(position).toString() + ", which is at cell position " + position);
        itemClickListener.onItemClick(position, view);

        transitionReenter = new Runnable() {
            @Override
            public void run() {
                if (((FilePreview) view).getFileInfo().isVideo()) {
                    ((FilePreview) view).appearContorllers();
                }
            }
        };


    }

    public void registerTransitionListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final Transition sharedElementEnterTransition = context.getWindow().getSharedElementReenterTransition();
            sharedElementEnterTransition.addListener(new TransitionListenerAdapter() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);
                    if (transitionReenter != null) {
                        transitionReenter.run();
                    }

                }
            });

        }
    }

    private ArrayList<FileInfo> getAllShownImagesPath() {
//            Uri uri;
//            Cursor cursor;
//            int column_index_data, column_index_folder_name;
//
//            String absolutePathOfImage = null;
//            uri = MediaStore.Images.Media.E;
//
//
//
//            cursor = activity.getContext().getContentResolver().query(uri, projection, null,
//                    null, null);
//
//            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
//            column_index_folder_name = cursor
//                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
//            while (cursor.moveToNext()) {
//                absolutePathOfImage = cursor.getString(column_index_data);
//
//                listOfAllMedia.add(absolutePathOfImage);
//            }
//
//        ArrayList<String> listOfAllMedia = new ArrayList<String>();
//        String[] projection = {MediaStore.MediaColumns.DATA};
//        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,null, null);
//
//        while (cursor.moveToNext()) {
//            String absolutePathOfImage = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
//
//            listOfAllMedia.add(absolutePathOfImage);
//        }
//        cursor.close();

        ArrayList<FileInfo> listOfAllMedia = new ArrayList<FileInfo>();
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE
        };

// Return only video and image metadata.
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;


        Uri queryUri = MediaStore.Files.getContentUri("external");

        CursorLoader cursorLoader = new CursorLoader(
                context,
                queryUri,
                projection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );

        Cursor cursor = cursorLoader.loadInBackground();
        while (cursor.moveToNext()) {
            String absolutePathOfImage = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE));

            if (mimeType.startsWith("image") || mimeType.startsWith("video")) {
                listOfAllMedia.add(new FileInfo(title, absolutePathOfImage, mimeType));
            }
        }
        cursor.close();


        return listOfAllMedia;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public FilePreview fileParingImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            FilePreview picturesView = (FilePreview) itemView;


            //  picturesView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400));

            fileParingImageView = picturesView;
            picturesView.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {
            onItemClick(view, getAdapterPosition());
        }
    }
}
