package com.diraapp.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.requests.RoomUpdateRequest;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.images.ImagesWorker;
import com.diraapp.ui.adapters.MediaGridItemListener;
import com.diraapp.ui.bottomsheet.ServerSelectorBottomSheet;
import com.diraapp.ui.bottomsheet.filepicker.FilePickerBottomSheet;
import com.diraapp.ui.components.DiraPopup;
import com.diraapp.ui.components.MediaGridItem;
import com.diraapp.utils.ImageRotationFix;
import com.diraapp.utils.SliderActivity;

import java.io.File;
import java.io.IOException;

public class EditRoomActivity extends DiraActivity implements ServerSelectorBottomSheet.BottomSheetListener {

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

        findViewById(R.id.room_server).setOnClickListener((View v) -> {
            DiraPopup diraPopup = new DiraPopup(this);
            diraPopup.show(getString(R.string.edit_room_server_popup_title),
                    getString(R.string.edit_room_server_popup_text), null,
                    null, new Runnable() {
                        @Override
                        public void run() {
                            ServerSelectorBottomSheet serverSelectorBottomSheet = new ServerSelectorBottomSheet();
                            serverSelectorBottomSheet.show(getSupportFragmentManager(), "Server selector  bottom sheet");
                        }
                    });
        });

        findViewById(R.id.button_change_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        EditText updatesLifetimeInput = findViewById(R.id.room_updates_lifetime);

        findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText roomName = findViewById(R.id.room_name);


                int sec = Integer.parseInt(updatesLifetimeInput.getText().toString()) * 60 * 60;
                room.setUpdateExpireSec(sec);
                RoomUpdateRequest request = new RoomUpdateRequest(AppStorage.getBase64FromBitmap(roomPicture),
                        roomName.getText().toString(), roomSecret, sec);

                try {
                    UpdateProcessor.getInstance().sendRequest(request, room.getServerAddress());
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
        if (resultCode != RESULT_OK && resultCode != MediaSendActivity.CODE) {
            return;
        }
        if (requestCode == 2) {
            final Bundle extras = data.getExtras();
            if (extras != null) {


            }
        }
        if (resultCode == MediaSendActivity.CODE) {
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

            String imageUri = data.getStringArrayListExtra("uris").get(0);


            updatePicture(imageUri);
        }
    }

    public void updatePicture(String path) {
        roomPicture = ImagesWorker.getCircleCroppedBitmap(AppStorage.getBitmapFromPath(path), 256, 256);
        roomPicture = ImagesWorker.compressBitmap(roomPicture);


        try {
            roomPicture = ImageRotationFix.rotateImageIfRequired(getApplicationContext(), roomPicture, Uri.fromFile(new File(path)));
        } catch (IOException e) {
            roomPicture = AppStorage.getBitmapFromPath(path);
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
        filePickerBottomSheet.setOnlyImages(true);
        filePickerBottomSheet.show(getSupportFragmentManager(), "blocked");
        filePickerBottomSheet.setRunnable(new MediaGridItemListener() {
            @Override
            public void onItemClick(int pos, final View view) {
                MediaSendActivity.open(EditRoomActivity.this, filePickerBottomSheet.getMedia().get(pos).getFilePath(), "",
                        (MediaGridItem) view, MediaSendActivity.IMAGE_PURPOSE_SELECT);

            }

            @Override
            public void onLastItemLoaded(int pos, View view) {

            }
        });

    }

    private void loadData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                EditRoomActivity.this.room = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getRoomBySecretName(roomSecret);

                int hours = room.getUpdateExpireSec() / 60 / 60;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (room.getImagePath() != null) {
                            ImageView roomPicture = findViewById(R.id.room_picture);
                            roomPicture.setImageBitmap(AppStorage.getBitmapFromPath(room.getImagePath()));
                        }
                        TextView textView = findViewById(R.id.room_server);
                        textView.setText(room.getServerAddress());

                        if (hours != 0) {
                            TextView updatesLifetimeInput = findViewById(R.id.room_updates_lifetime);
                            updatesLifetimeInput.setText(String.valueOf(hours));
                        }

                        EditText roomName = findViewById(R.id.room_name);
                        roomName.setText(room.getName());
                    }
                });
            }
        });
        thread.start();
    }

    @Override
    public void onServerSelected(String serverAddress) {
        TextView textView = findViewById(R.id.room_server);
        textView.setText(serverAddress);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                EditRoomActivity.this.room = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getRoomBySecretName(roomSecret);
                room.setServerAddress(serverAddress);
                DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().update(room);
            }
        });
        thread.start();

    }
}