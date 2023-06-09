package com.diraapp.ui.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.diraapp.R;
import com.diraapp.databinding.ActivityMemoryManagementBinding;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.FileClassifier;
import com.diraapp.utils.SliderActivity;

import java.io.File;

public class MemoryManagementActivity extends AppCompatActivity {

    private long imagesSize = 0;
    private long videosSize = 0;

    private ActivityMemoryManagementBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMemoryManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener((View v) -> {
            onBackPressed();
        });

        binding.buttonDeleteImages.setOnClickListener((View v) -> {
            delete(AttachmentType.IMAGE, v);
        });

        binding.buttonDeleteVideos.setOnClickListener((View v) -> {
            delete(AttachmentType.VIDEO, v);
        });

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        calculateUsedSpace();
    }

    private void calculateUsedSpace() {
        binding.progressCircular.setVisibility(View.VISIBLE);
        binding.totalUsedText.setText(getString(R.string.memory_management_loading));

        videosSize = 0;
        imagesSize = 0;

        Thread calculatingThread = new Thread(() -> {
            for (File file : getExternalCacheDir().listFiles()) {
                if (file.isFile()) {

                    if (FileClassifier.isImageFile(file.getPath())) {
                        imagesSize += file.length();
                    } else {
                        videosSize += file.length();
                    }
                }
            }

            runOnUiThread(() -> {
                binding.progressCircular.setVisibility(View.GONE);

                binding.imageSizeText.setText(AppStorage.getStringSize(imagesSize));
                binding.videoSizeText.setText(AppStorage.getStringSize(videosSize));
                binding.totalUsedText.setText(getString(R.string.memory_management_total_used)
                        .replace("%s", AppStorage.getStringSize(videosSize + imagesSize)));
            });
        });
        calculatingThread.start();
    }

    private void delete(AttachmentType attachmentType, View buttonLayout) {
        buttonLayout.setEnabled(false);
        buttonLayout.setAlpha(0.5f);

        Thread deletingThread = new Thread(() -> {

            for (File file : getExternalCacheDir().listFiles()) {
                if (file.isFile()) {

                    if (FileClassifier.isImageFile(file.getPath())) {
                        if (attachmentType == AttachmentType.IMAGE) {
                            file.delete();
                        }
                    } else {
                        if (attachmentType == AttachmentType.VIDEO) {
                            file.delete();
                        }
                    }
                }

            }

            runOnUiThread(() -> {
                calculateUsedSpace();
                buttonLayout.setEnabled(true);
                buttonLayout.setAlpha(1.0f);
            });
        });

        deletingThread.start();
    }
}