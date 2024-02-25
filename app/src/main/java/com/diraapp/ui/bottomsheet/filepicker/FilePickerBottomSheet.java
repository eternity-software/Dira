package com.diraapp.ui.bottomsheet.filepicker;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.BuildConfig;
import com.diraapp.R;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.room.RoomActivity;
import com.diraapp.ui.adapters.GridItemsSpacingDecorator;
import com.diraapp.ui.adapters.MediaGridItemListener;
import com.diraapp.ui.adapters.MediaGridAdapter;
import com.diraapp.ui.waterfalls.WaterfallBalancer;
import com.diraapp.utils.android.DeviceUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;


public class FilePickerBottomSheet extends BottomSheetDialogFragment {
    private static RecyclerView.RecycledViewPool recycledViewPool;
    FrameLayout bottomSheet;
    private View view;

    private EditText messageInput;
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
    private MultiFilesListener multiFilesListener;

    private String messageText;

    private int inputHeight = 0;

    private boolean isInputContainerShown = false;


    public List<SelectorFileInfo> getMedia() {
        return mediaGridAdapter.getMediaElements();
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        super.show(manager, tag);
    }

    public void setMultiFilesListener(MultiFilesListener multiFilesListener) {
        this.multiFilesListener = multiFilesListener;
    }

    public void setOnlyImages(boolean onlyImages) {
        this.onlyImages = onlyImages;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (recycledViewPool == null) {
            recycledViewPool = new RecyclerView.RecycledViewPool();
        }
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        View v2 = inflater.inflate(R.layout.bottom_sheet_filespicker, container, true);
        view = v2;


        RecyclerView recyclerView = view.findViewById(R.id.recycler);

        recyclerView.setRecycledViewPool(recycledViewPool);
        recyclerView.addItemDecoration(new GridItemsSpacingDecorator(
                DeviceUtils.dpToPx(2, getContext()),
                3));
        messageInput = view.findViewById(R.id.message_box);
        if (messageText != null)
            messageInput.setText(messageText);

        inputContainer = view.findViewById(R.id.linearLayout3);

        // calc inputContainer height
        ViewTreeObserver vto = inputContainer.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                inputContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                inputHeight = inputContainer.getMeasuredHeight();

                inputContainer.getLayoutParams().height = 0;
                inputContainer.requestLayout();
            }
        });


        view.findViewById(R.id.sendButton).setOnClickListener(v -> {
            multiFilesListener.onSelectedFilesSent(mediaGridAdapter.getSelectedFiles(), messageInput.getText().toString());
            dismiss();
        });

        final TextView debugText = view.findViewById(R.id.debugText);
        mediaGridAdapter = new MediaGridAdapter((DiraActivity) getActivity(), onItemClickListener, recyclerView, onlyImages,
                (files, buckets) -> {

                    Spinner staticSpinner = view.findViewById(R.id.album_picker);
                    buckets.add(0, getString(R.string.media_gallery));

                    for (int i = 0; i < buckets.size(); i++) {
                        String s = buckets.get(i);
                        if (s == null) {
                            buckets.remove(i);
                        }
                    }

                    // Create an ArrayAdapter using the string array and a default spinner
                    String[] stringArray = new String[buckets.size()];
                    stringArray = buckets.toArray(stringArray);
                    ArrayAdapter<String> staticAdapter = new ArrayAdapter<String>(getContext(),
                            R.layout.spinner_row, R.id.spinner_text, stringArray);


                    // Apply the adapter to the spinner
                    staticSpinner.setAdapter(staticAdapter);
                    staticSpinner.setSelection(0, false);

                    staticSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view,
                                                   int position, long id) {
                            Log.v("item", (String) parent.getItemAtPosition(position));
                            if (position == 0) {
                                mediaGridAdapter.loadForBucket(null);
                            } else {
                                mediaGridAdapter.loadForBucket((String) parent.getItemAtPosition(position));
                                BottomSheetBehavior.from(bottomSheet)
                                        .setState(BottomSheetBehavior.STATE_EXPANDED);
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // TODO Auto-generated method stub
                        }
                    });

                });
        mediaGridAdapter.setMultiSelect(isMultiSelection);
        if (BuildConfig.DEBUG)
            debugText.setVisibility(View.VISIBLE);
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

        initDocumentPickerButton();

        recyclerView.setAdapter(mediaGridAdapter);
        v2.findViewById(R.id.openCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(getActivity(), CameraActivity.class);

                getActivity().startActivity(intent);*/

            }
        });

        return v2;
    }


    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setMultiSelection(boolean isMultiSelect) {
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
                onItemClickListener.onLastItemLoaded(pos, view);
            }

            @Override
            public void onItemSelected(SelectorFileInfo diraMediaInfo,
                                       List<SelectorFileInfo> diraMediaInfoList) {
                if (diraMediaInfoList.size() == 0) {
                    inputHeight = inputContainer.getHeight();
                    hideEditText();
//                    inputContainer.setVisibility(View.GONE);
                } else {

                    if (multiFilesListener != null) {
                        showEditText();
                        //inputContainer.setVisibility(View.VISIBLE);
                        BottomSheetBehavior.from(bottomSheet)
                                .setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
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

    private void initDocumentPickerButton() {
        final ImageView documentPickerButton = view.findViewById(R.id.documents_picker);
        if (onlyImages) {
            documentPickerButton.setVisibility(View.GONE);
            return;
        }

        documentPickerButton.setOnClickListener((View v) -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

            intent.putExtra("text", messageInput.getText());

            try {
                getActivity().startActivityForResult(
                        Intent.createChooser(intent, "Select a File to Upload"),
                        RoomActivity.SEND_FILE_CODE);
            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog
//                Toast.makeText(this, "Please install a File Manager.",
//                        Toast.LENGTH_SHORT).show();
            }

            dismiss();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);


    }

    @Override
    public void onStart() {
        super.onStart();

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


                float pix = DeviceUtils.dpToPx(20, getContext()) * (1 - slideOffset);
                float maxPix = DeviceUtils.dpToPx(20, getContext());

                if (pix < maxPix) {
                    cardView.setRadius(pix);
                } else {
                    cardView.setRadius(maxPix);
                }


            }
        });

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

    private void showEditText() {
        if (isInputContainerShown) return;

        if (getActivity() == null) return;

        ((DiraActivity) getActivity()).performHeightAnimation(0, inputHeight, inputContainer);
        isInputContainerShown = true;
    }

    private void hideEditText() {
        if (!isInputContainerShown) return;

        if (getActivity() == null) return;

        ((DiraActivity) getActivity()).performHeightAnimation(inputHeight, 0, inputContainer);
        isInputContainerShown = false;

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public interface MultiFilesListener {
        void onSelectedFilesSent(List<SelectorFileInfo> diraMediaInfoList, String messageText);

    }
}
