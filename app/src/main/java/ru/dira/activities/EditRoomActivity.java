package ru.dira.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import ru.dira.R;
import ru.dira.api.requests.RoomUpdateRequest;
import ru.dira.attachments.ImageStorage;
import ru.dira.db.DiraRoomDatabase;
import ru.dira.db.entities.Room;
import ru.dira.exceptions.UnablePerformRequestException;
import ru.dira.services.UpdateProcessor;
import ru.dira.utils.SliderActivity;

public class EditRoomActivity extends AppCompatActivity {

    public static final String ROOM_SECRET_EXTRA = "roomSecret";

    private String roomSecret;
    private Room room;

    private Bitmap roomPicture;

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
                RoomUpdateRequest request = new RoomUpdateRequest(ImageStorage.getBase64FromBitmap(roomPicture), roomName.getText().toString(), roomSecret);

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
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 1) {
            final Bundle extras = data.getExtras();
            if (extras != null) {
                roomPicture = extras.getParcelable("data");


                ImageView imageView = findViewById(R.id.room_picture);
                imageView.setImageBitmap(roomPicture);



            }
        }
    }


    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("outputX", 256);
        intent.putExtra("outputY", 256);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 1);
    }

    private void loadData()
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                EditRoomActivity.this.room = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getRoomBySecretName(roomSecret);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(room.getImagePath() != null)
                        {
                            ImageView roomPicture = findViewById(R.id.room_picture);
                            roomPicture.setImageBitmap(ImageStorage.getImage(room.getImagePath()));
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