package com.diraapp.ui.activities;

import static com.diraapp.storage.AppStorage.getRealPathFromURI;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.diraapp.R;
import com.diraapp.ui.adapters.MediaGridItemListener;
import com.diraapp.api.requests.UpdateMemberRequest;
import com.diraapp.ui.bottomsheet.filepicker.FilePickerBottomSheet;
import com.diraapp.ui.components.FilePreview;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Room;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.images.ImagesWorker;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.ImageRotationFix;
import com.diraapp.utils.SliderActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PersonalityActivity extends AppCompatActivity {

    private Bitmap userPicture;
    private FilePickerBottomSheet filePickerBottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personality);

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        EditText nicknameText = findViewById(R.id.nickname_text);
        EditText idText = findViewById(R.id.id_text);

        ImageView imageView = findViewById(R.id.profile_picture);

        CacheUtils cacheUtils = new CacheUtils(getApplicationContext());

        String picPath = cacheUtils.getString(CacheUtils.PICTURE);
        if (picPath != null) imageView.setImageBitmap(AppStorage.getBitmapFromPath(picPath));

        nicknameText.setText(cacheUtils.getString(CacheUtils.NICKNAME));
        idText.setText(cacheUtils.getString(CacheUtils.ID));

        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread save = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<String> roomSecrets = new ArrayList<>();

                        for (Room room : DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getAllRoomsByUpdatedTime()) {
                            roomSecrets.add(room.getSecretName());


                            UpdateMemberRequest updateMemberRequest = new UpdateMemberRequest(nicknameText.getText().toString(),
                                    AppStorage.getBase64FromBitmap(userPicture), roomSecrets, idText.getText().toString(), System.currentTimeMillis());

                            try {
                                UpdateProcessor.getInstance().sendRequest(updateMemberRequest, room.getServerAddress());
                            } catch (UnablePerformRequestException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                cacheUtils.setString(CacheUtils.NICKNAME, nicknameText.getText().toString());
                                cacheUtils.setString(CacheUtils.ID, idText.getText().toString());
                                String path = AppStorage.saveToInternalStorage(userPicture, "userpic.png", null, getApplicationContext());
                                cacheUtils.setString(CacheUtils.PICTURE, path);
                                finish();
                            }
                        });
                    }
                });
                save.start();

            }
        });


        findViewById(R.id.button_change_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        findViewById(R.id.button_settings).setOnClickListener((View v) -> {
            Intent intent = new Intent(PersonalityActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK && resultCode != ImageSendActivity.CODE) {
            return;
        }
        if (requestCode == 2) {
            final Bundle extras = data.getExtras();
            if (extras != null) {
                userPicture = extras.getParcelable("data");


            }
        }
        if (resultCode == ImageSendActivity.CODE) {
            if (filePickerBottomSheet != null) {

                /**
                 * Throws an java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
                 * on some devices (tested on Android 5.1)
                 */
                try {
                    filePickerBottomSheet.dismiss();
                } catch (Exception ignored) {
                }
            }
            final String message = data.getStringExtra("text");
            String imageUri = data.getStringExtra("uri");


            try {
                updateProfilePhoto(getRealPathFromURI(this, Uri.parse(imageUri)));
            } catch (Exception e) {
                updateProfilePhoto(imageUri);
            }


        }
    }


    public void updateProfilePhoto(String path) {
        userPicture = ImagesWorker.getCircleCroppedBitmap(AppStorage.getBitmapFromPath(path), 256, 256);
        userPicture = ImagesWorker.compressBitmap(userPicture);


        try {
            userPicture = ImageRotationFix.rotateImageIfRequired(getApplicationContext(), userPicture, Uri.parse(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageView imageView = findViewById(R.id.profile_picture);
        imageView.setImageBitmap(userPicture);

    }

    public void pickImage() {
    /*    Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("outputX", 256);
        intent.putExtra("outputY", 256);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 1);*/
        filePickerBottomSheet = new FilePickerBottomSheet();
        filePickerBottomSheet.show(getSupportFragmentManager(), "blocked");
        filePickerBottomSheet.setRunnable(new MediaGridItemListener() {
            @Override
            public void onItemClick(int pos, final View view) {
                ImageSendActivity.open(PersonalityActivity.this, filePickerBottomSheet.getMedia().get(pos).getFilePath(), "",
                        (FilePreview) view, ImageSendActivity.IMAGE_PURPOSE_SELECT);


            }
        });

    }
}