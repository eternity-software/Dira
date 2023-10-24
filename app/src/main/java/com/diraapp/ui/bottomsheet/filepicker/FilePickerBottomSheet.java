package com.diraapp.ui.bottomsheet.filepicker;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.storage.images.WaterfallBalancer;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.adapters.MediaGridAdapter;
import com.diraapp.ui.adapters.MediaGridItemListener;
import com.diraapp.utils.Numbers;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;


public class FilePickerBottomSheet extends BottomSheetDialogFragment {
    private View view;
    private ArrayList<String> images;
    private MediaGridAdapter mediaGridAdapter;

    /**
     * This will not work properly with FragmentManager
     * <p>
     * Must be initialized onAttach as context
     */
    private MediaGridItemListener onItemClickListener;
    private Runnable onDismiss;

    private boolean onlyImages = false;
    private boolean isMultiSelection = false;

    private View inputContainer;

    FrameLayout bottomSheet;


    public ArrayList<SelectorFileInfo> getMedia() {
        return mediaGridAdapter.getMediaElements();
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        super.show(manager, tag);
    }

    public void setOnlyImages(boolean onlyImages) {
        this.onlyImages = onlyImages;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.bottom_sheet_filespicker, container, true);
        view = v;

        v.findViewById(R.id.openCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(getActivity(), CameraActivity.class);

                getActivity().startActivity(intent);*/

            }
        });

        return v;
    }

    public void setMultiSelection(boolean isMultiSelect)
    {
        isMultiSelection = isMultiSelect;
    }

    public void setRunnable(MediaGridItemListener onItemClickListener) {
        this.onItemClickListener = new MediaGridItemListener() {
            @Override
            public void onItemClick(int pos, View view) {
                onItemClickListener.onItemClick(pos, view);
            }

            @Override
            public void onLastItemLoaded(int pos, View view) {
                onItemClickListener.onItemClick(pos, view);
            }

            @Override
            public void onItemSelected(SelectorFileInfo selectorFileInfo,
                                       List<SelectorFileInfo> selectorFileInfoList) {
                if(selectorFileInfoList.size() == 0)
                {
                    inputContainer.setVisibility(View.GONE);
                }
                else
                {

                    inputContainer.setVisibility(View.VISIBLE);
                    BottomSheetBehavior.from(bottomSheet)
                            .setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Пустой фон
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    public void setOnDismiss(Runnable onDismiss) {
        this.onDismiss = onDismiss;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);


    }

    @Override
    public void onStart() {
        super.onStart();

        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();


        bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);


        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);


        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                final CardView cardView = view.findViewById(R.id.appBar);


                float pix = Numbers.dpToPx(20, getContext()) * (1 - slideOffset);
                float maxPix = Numbers.dpToPx(20, getContext());

                if (pix < maxPix) {
                    cardView.setRadius(pix);
                } else {
                    cardView.setRadius(maxPix);
                }


            }
        });
        setAllowReturnTransitionOverlap(false);
        setAllowEnterTransitionOverlap(false);
        RecyclerView gallery = bottomSheet.findViewById(R.id.gridView);
        gallery.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        inputContainer = bottomSheet.findViewById(R.id.linearLayout3);

        bottomSheet.findViewById(R.id.sendButton).setOnClickListener(v -> {

        });

        final TextView debugText = view.findViewById(R.id.debugText);
        mediaGridAdapter = new MediaGridAdapter((DiraActivity) getActivity(), onItemClickListener, gallery, onlyImages);
        mediaGridAdapter.setMultiSelect(isMultiSelection);
        mediaGridAdapter.setBalancerCallback(new WaterfallBalancer.BalancerCallback() {
            @Override
            public void onActiveWaterfallsCountChange(final int count) {
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            debugText.setText("Active waterfalls: " + count);
                        }
                    });
                } catch (Exception ignored) {

                }

            }
        });

        gallery.setAdapter(mediaGridAdapter);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if (onDismiss != null) onDismiss.run();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (onDismiss != null) onDismiss.run();


    }
}
