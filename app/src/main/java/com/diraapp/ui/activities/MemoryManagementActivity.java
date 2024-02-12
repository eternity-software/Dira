package com.diraapp.ui.activities;

import android.os.Bundle;
import android.view.View;

import com.diraapp.R;
import com.diraapp.databinding.ActivityMemoryManagementBinding;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.FileClassifier;
import com.diraapp.utils.SliderActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MemoryManagementActivity extends DiraActivity {

    private long imagesSize = 0;
    private long videosSize = 0;
    private long unknownTypeSize = 0;
    private long profilePicSize = 0;
    private long totalSize = 0;

    private ActivityMemoryManagementBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMemoryManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonBack.setOnClickListener((View v) -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        binding.buttonDeleteImages.setOnClickListener((View v) -> {
            delete(StoredFileType.IMAGE, v);
        });

        binding.buttonDeleteVideos.setOnClickListener((View v) -> {
            delete(StoredFileType.VIDEO, v);
        });

        binding.buttonDeleteProfilePics.setOnClickListener((View v) -> {
            delete(StoredFileType.PROFILE_PIC, v);
        });

        binding.buttonDeleteUnknown.setOnClickListener((View v) -> {
            delete(StoredFileType.UNKNOWN, v);
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
        profilePicSize = 0;
        unknownTypeSize = 0;
        totalSize = 0;


        Thread calculatingThread = new Thread(() -> {


            parseDataDirs(null);



            runOnUiThread(() -> {
                binding.progressCircular.setVisibility(View.GONE);

                binding.imageSizeText.setText(AppStorage.getStringSize(imagesSize));
                binding.videoSizeText.setText(AppStorage.getStringSize(videosSize));
                binding.profilePicSizeText.setText(AppStorage.getStringSize(profilePicSize));
                binding.unknownSizeText.setText(AppStorage.getStringSize(unknownTypeSize));
                binding.totalUsedText.setText(getString(R.string.memory_management_total_used)
                        .replace("%s", AppStorage.getStringSize(totalSize)));
            });
        });
        calculatingThread.start();
    }

    public void parseDataDirs(StoredFileType deleteAttachmentType)
    {
        List<File> cacheLocations = new ArrayList<>();

        cacheLocations.add(getCacheDir());
        cacheLocations.add(getExternalCacheDir());
        cacheLocations.add(new File(getApplicationInfo().dataDir));


        for (File cacheDir : cacheLocations) {
            totalSize += parseDataDir(cacheDir, deleteAttachmentType);
        }
    }

    public long parseDataDir(File dir, StoredFileType deleteAttachmentType) {
        long size = 0;
        if (dir == null) return 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += parseDataDir(file, deleteAttachmentType);
            } else if (file != null && file.isFile()) {
                size += file.length();
                if (FileClassifier.isImageFile(file.getPath())) {


                    boolean isProfilePic = getExternalCacheDir().getAbsolutePath().equals(file.getParentFile().getAbsolutePath());
                    if((deleteAttachmentType == StoredFileType.IMAGE && !isProfilePic) ||
                            (deleteAttachmentType == StoredFileType.PROFILE_PIC && isProfilePic))
                    {
                        file.delete();
                    }

                    if(isProfilePic)
                    {
                        profilePicSize += file.length();
                    }
                    else
                    {
                        imagesSize += file.length();
                    }

                } else {
                    if(FileClassifier.isVideoFile(file.getPath(), getApplicationContext()))
                    {
                        if(deleteAttachmentType == StoredFileType.VIDEO)
                        {
                            file.delete();
                        }
                        videosSize += file.length();
                    }
                    else if(FileClassifier.isDiraUnknownType(file.getPath()))
                    {
                        if(deleteAttachmentType == StoredFileType.UNKNOWN)
                        {
                            file.delete();
                        }
                        unknownTypeSize += file.length();
                    }



                }
            }
        }
        return size;
    }

    private void delete(StoredFileType attachmentType, View buttonLayout) {
        buttonLayout.setEnabled(false);
        buttonLayout.setAlpha(0.5f);

        Thread deletingThread = new Thread(() -> {
            parseDataDirs(attachmentType);

            runOnUiThread(() -> {
                calculateUsedSpace();
                buttonLayout.setEnabled(true);
                buttonLayout.setAlpha(1.0f);
            });
        });

        deletingThread.start();
    }

    public enum StoredFileType
    {
        VIDEO,
        IMAGE,
        PROFILE_PIC,
        UNKNOWN
    }
}