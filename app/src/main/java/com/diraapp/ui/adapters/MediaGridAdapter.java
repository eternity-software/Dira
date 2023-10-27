package com.diraapp.ui.adapters;

import android.annotation.SuppressLint;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.storage.images.WaterfallBalancer;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.anim.BounceInterpolator;
import com.diraapp.storage.DiraMediaInfo;
import com.diraapp.ui.bottomsheet.filepicker.SelectorFileInfo;
import com.diraapp.ui.components.MediaGridItem;
import com.diraapp.utils.android.DiraVibrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MediaGridAdapter extends RecyclerView.Adapter<MediaGridAdapter.ViewHolder> {


    private final int threadCount = 0;
    private final LayoutInflater mInflater;
    private final WaterfallBalancer waterfallBalancer;
    private final DiraActivity context;
    private final MediaGridItemListener itemClickListener;
    private ArrayList<SelectorFileInfo> mediaElements = new ArrayList<>();
    private Runnable transitionReenter;

    private boolean multiSelect = false;

    private List<SelectorFileInfo> selectedFiles = new ArrayList<>();
    private HashMap<MediaGridItem, SelectorFileInfo> selectedViews = new HashMap<>();

    private boolean isSelected = false;

    /**
     * Constructor for custom files arrays
     *
     * @param context
     * @param itemClickListener
     * @param recyclerView
     */
    public MediaGridAdapter(final DiraActivity context, ArrayList<SelectorFileInfo> mediaElements, MediaGridItemListener itemClickListener, RecyclerView recyclerView) {
        this.mInflater = LayoutInflater.from(context);
        this.itemClickListener = itemClickListener;
        this.context = context;

        this.mediaElements = mediaElements;
        //   Collections.reverse(images);


        waterfallBalancer = new WaterfallBalancer(context);


    }

    /**
     * Constructor to get whole device memory
     *
     * @param context
     * @param itemClickListener
     * @param recyclerView
     */
    public MediaGridAdapter(final DiraActivity context, MediaGridItemListener itemClickListener, RecyclerView recyclerView,
                            boolean onlyImages) {
        this.mInflater = LayoutInflater.from(context);
        this.itemClickListener = itemClickListener;
        this.context = context;


        CursorLoader cursorLoader = getCursorLoader(onlyImages);

        // Must be executed on new Thread
        mediaElements = loadGallery(cursorLoader.loadInBackground());
        DiraActivity.runOnMainThread(() ->
                notifyDataSetChanged());


        //   Collections.reverse(images);
        waterfallBalancer = new WaterfallBalancer(context);


    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }



    public void setBalancerCallback(WaterfallBalancer.BalancerCallback balancerCallback) {
        waterfallBalancer.setBalancerCallback(balancerCallback);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.file_picker_image, parent, false);

        return new ViewHolder(view);
    }

    public void updateExistingSelectedViews() {
        for (MediaGridItem mediaGridItem : selectedViews.keySet()) {
            SelectorFileInfo diraMediaInfo = selectedViews.get(mediaGridItem);
            mediaGridItem.updateUi(diraMediaInfo.isSelected(), selectedFiles.indexOf(diraMediaInfo));
        }
    }

    public List<SelectorFileInfo> getSelectedFiles() {
        return selectedFiles;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MediaGridItem picturesView = holder.fileParingImageView;
        SelectorFileInfo diraMediaInfo = mediaElements.get(position);
        picturesView.setFileInfo(diraMediaInfo);


        waterfallBalancer.remove(picturesView);
        try {

            picturesView.getFileParingImageView().setBackgroundColor(Theme.getColor(context, R.color.dark));
            picturesView.getFileParingImageView().setImageDrawable(null);

            if (multiSelect) {
                selectedViews.remove(picturesView);
                if (diraMediaInfo.isSelected()) {
                    selectedViews.put(picturesView, diraMediaInfo);
                }
                picturesView.updateUi(diraMediaInfo.isSelected(), selectedFiles.indexOf(diraMediaInfo));
                picturesView.getSelectionTextButton().setVisibility(View.VISIBLE);
                picturesView.getSelectionTextContainer().setOnClickListener(v -> {

                    if (!diraMediaInfo.isSelected()) {
                        if (selectedFiles.size() < 10) {
                            diraMediaInfo.setSelected(true);
                            selectedFiles.add(diraMediaInfo);

                            selectedViews.put(picturesView, diraMediaInfo);
                            final Animation animation = AnimationUtils.loadAnimation(context, R.anim.bounce);

                            // Use bounce interpolator with amplitude 0.1 and frequency 15
                            BounceInterpolator interpolator = new BounceInterpolator(0.5, 2);
                            animation.setInterpolator(interpolator);
                            picturesView.startAnimation(animation);
                            updateExistingSelectedViews();
                            itemClickListener.onItemSelected(diraMediaInfo, selectedFiles);
                        } else {
                            DiraVibrator.vibrateOneTime(context);
                        }

                    } else {
                        diraMediaInfo.setSelected(false);
                        selectedViews.remove(picturesView);
                        selectedFiles.remove(diraMediaInfo);
                        updateExistingSelectedViews();
                        itemClickListener.onItemSelected(diraMediaInfo, selectedFiles);
                    }

                    picturesView.updateUi(diraMediaInfo.isSelected(), selectedFiles.indexOf(diraMediaInfo));
                });
            } else {
                picturesView.getSelectionTextButton().setVisibility(View.GONE);
            }

            waterfallBalancer.add(picturesView);

            if (position == mediaElements.size() - 1) {
                itemClickListener.onLastItemLoaded(position, holder.fileParingImageView);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mediaElements.size();
    }

    public ArrayList<SelectorFileInfo> getMediaElements() {
        return mediaElements;
    }

    // Convenience method for getting data at click position
    public DiraMediaInfo getItem(int id) {
        return mediaElements.get(id);
    }

    // Method that executes your code for the action received
    public void onItemClick(final View view, int position) {
        Log.i("TAG", "You clicked number " + getItem(position).toString() + ", which is at cell position " + position);
        itemClickListener.onItemClick(position, view);

        transitionReenter = new Runnable() {
            @Override
            public void run() {
                if (((MediaGridItem) view).getFileInfo().isVideo()) {
                    ((MediaGridItem) view).appearContorllers();
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

    @SuppressLint("Range")
    private ArrayList<SelectorFileInfo> loadGallery(Cursor cursor) {
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


        ArrayList<SelectorFileInfo> listOfAllMedia = new ArrayList<SelectorFileInfo>();
        while (cursor.moveToNext()) {
            String absolutePathOfImage = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE));

            if (mimeType.startsWith("image") || mimeType.startsWith("video")) {
                listOfAllMedia.add(new SelectorFileInfo(title, absolutePathOfImage, mimeType));
            }
        }
        cursor.close();


        return listOfAllMedia;
    }

    private CursorLoader getCursorLoader(boolean isOnlyImages) {
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

        if (isOnlyImages) {
            selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
        }


        Uri queryUri = MediaStore.Files.getContentUri("external");

        CursorLoader cursorLoader = new CursorLoader(
                context,
                queryUri,
                projection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );

        return cursorLoader;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public MediaGridItem fileParingImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            MediaGridItem picturesView = (MediaGridItem) itemView;


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
