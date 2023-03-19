package ru.dira.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import ru.dira.R;
import ru.dira.api.requests.RoomUpdateRequest;
import ru.dira.bottomsheet.filepicker.FilePickerBottomSheet;
import ru.dira.components.FilePreview;
import ru.dira.db.DiraRoomDatabase;
import ru.dira.db.entities.Room;
import ru.dira.exceptions.UnablePerformRequestException;
import ru.dira.storage.AppStorage;
import ru.dira.storage.images.ImagesWorker;
import ru.dira.updates.UpdateProcessor;
import ru.dira.utils.ImageRotationFix;
import ru.dira.utils.SliderActivity;

public class EditRoomActivity extends AppCompatActivity {

    public static final String ROOM_SECRET_EXTRA = "roomSecret";

    private String roomSecret;
    private Room room;

    private Bitmap roomPicture;
    private FilePickerBottomSheet filePickerBottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_room);

        roomSecret = getIntent().getExtras().getString(ROOM_SECRET_EXTRA);

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        loadData();

        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.button_change_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText roomName = findViewById(R.id.room_name);
                RoomUpdateRequest request = new RoomUpdateRequest(AppStorage.getBase64FromBitmap(roomPicture), roomName.getText().toString(), roomSecret);

                try {
                    UpdateProcessor.getInstance().sendRequest(request);
                } catch (UnablePerformRequestException e) {
                    e.printStackTrace();
                }
                finish();
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

            String imageUri = data.getStringExtra("uri");


            updatePicture(imageUri);


            // TODO: Upload image to server
        }
    }

    public void updatePicture(String path) {
        roomPicture = ImagesWorker.getCircleCroppedBitmap(AppStorage.getImage(path), 256, 256);
        roomPicture = ImagesWorker.compressBitmap(roomPicture);


        try {
            roomPicture = ImageRotationFix.rotateImageIfRequired(getApplicationContext(), roomPicture, Uri.parse(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageView imageView = findViewById(R.id.room_picture);
        imageView.setImageBitmap(roomPicture);

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
                ImageSendActivity.open(EditRoomActivity.this, filePickerBottomSheet.getMedia().get(pos).getFilePath(), "",
                        (FilePreview) view, ImageSendActivity.IMAGE_PURPOSE_SELECT);


            }
        });

    }

    private void loadData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                EditRoomActivity.this.room = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getRoomBySecretName(roomSecret);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (room.getImagePath() != null) {
                            ImageView roomPicture = findViewById(R.id.room_picture);
                            roomPicture.setImageBitmap(AppStorage.getImage(room.getImagePath()));
                        }

                        EditText roomName = findViewById(R.id.room_name);
                        roomName.setText(room.getName());
                    }
                });
            }
        });
        thread.start();
    }

}