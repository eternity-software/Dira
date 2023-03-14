package ru.dira.activities;

import static ru.dira.attachments.ImageStorage.getRealPathFromURI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.dira.BuildConfig;
import ru.dira.R;
import ru.dira.api.requests.UpdateMemberRequest;
import ru.dira.attachments.ImageStorage;
import ru.dira.attachments.ImagesWorker;
import ru.dira.bottomsheet.filepicker.FilePickerBottomSheet;
import ru.dira.components.FilePreview;
import ru.dira.db.DiraRoomDatabase;
import ru.dira.db.entities.Room;
import ru.dira.exceptions.UnablePerformRequestException;
import ru.dira.services.UpdateProcessor;
import ru.dira.utils.CacheUtils;
import ru.dira.utils.ImageRotationFix;
import ru.dira.utils.SliderActivity;

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

        TextView versionView = findViewById(R.id.version_view);
        versionView.setText(BuildConfig.VERSION_NAME + ", code " + BuildConfig.VERSION_CODE);

        ImageView imageView = findViewById(R.id.profile_picture);
        String picPath = CacheUtils.getInstance().getString(CacheUtils.PICTURE, getApplicationContext());
        if(picPath != null) imageView.setImageBitmap(ImageStorage.getImage(picPath));

        nicknameText.setText(CacheUtils.getInstance().getString(CacheUtils.NICKNAME, getApplicationContext()));
        idText.setText(CacheUtils.getInstance().getString(CacheUtils.ID, getApplicationContext()));

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

                        for(Room room : DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getAllRoomsByUpdatedTime())
                        {
                            roomSecrets.add(room.getSecretName());
                        }

                        UpdateMemberRequest updateMemberRequest = new UpdateMemberRequest(nicknameText.getText().toString(),
                                ImageStorage.getBase64FromBitmap(userPicture), roomSecrets,  idText.getText().toString(), System.currentTimeMillis());

                        try {
                            UpdateProcessor.getInstance().sendRequest(updateMemberRequest);
                        } catch (UnablePerformRequestException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CacheUtils.getInstance().setString(CacheUtils.NICKNAME, nicknameText.getText().toString(), getApplicationContext());
                                CacheUtils.getInstance().setString(CacheUtils.ID, idText.getText().toString(), getApplicationContext());
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



            // TODO: Upload image to server
        }
    }


   public void updateProfilePhoto(String path)
   {
       userPicture = ImagesWorker.getCircleCroppedBitmap(ImageStorage.getImage(path), 256, 256);
       userPicture = ImagesWorker.compressBitmap(userPicture);
       path = ImageStorage.saveToInternalStorage(userPicture, getApplicationContext());

       try {
           userPicture = ImageRotationFix.rotateImageIfRequired(getApplicationContext(), userPicture, Uri.parse(path));
       } catch (IOException e) {
           e.printStackTrace();
       }
       ImageView imageView = findViewById(R.id.profile_picture);
       imageView.setImageBitmap(userPicture);

       CacheUtils.getInstance().setString(CacheUtils.PICTURE, path, getApplicationContext());
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
        filePickerBottomSheet.setRunnable(new FilePickerBottomSheet.ItemClickListener() {
            @Override
            public void onItemClick(int pos, final View view) {
                ImageSendActivity.open(PersonalityActivity.this, filePickerBottomSheet.getMedia().get(pos).getFilePath(), "",
                        (FilePreview) view, ImageSendActivity.IMAGE_PURPOSE_SELECT);


            }
        });

    }
}