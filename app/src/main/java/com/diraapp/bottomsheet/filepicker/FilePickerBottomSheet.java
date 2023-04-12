package com.diraapp.bottomsheet.filepicker;

import android.annotation.SuppressLint;
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
import com.diraapp.utils.Numbers;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;


public class FilePickerBottomSheet extends BottomSheetDialogFragment {
    private final boolean isShown = false;
    private View view;
    private ArrayList<String> images;
    private FilePickerAdapter filePickerAdapter;
    private ItemClickListener onItemClickListener;

    public ArrayList<FileInfo> getMedia() {
        return filePickerAdapter.getImages();
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        super.show(manager, tag);

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

    public void setRunnable(ItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Пустой фон
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);


        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();


        final FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);


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


        final TextView debugText = view.findViewById(R.id.debugText);
        filePickerAdapter = new FilePickerAdapter(getActivity(), onItemClickListener, gallery);
        filePickerAdapter.setBalancerCallback(new WaterfallBalancer.BalancerCallback() {
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

        gallery.setAdapter(filePickerAdapter);


    }

    public interface ItemClickListener {
        void onItemClick(int pos, View view);
    }


}
