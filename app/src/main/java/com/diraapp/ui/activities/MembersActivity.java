package com.diraapp.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.ui.adapters.MembersAdapter;
import com.diraapp.ui.appearance.AppTheme;
import com.diraapp.ui.appearance.ColorTheme;
import com.diraapp.utils.SliderActivity;

import java.util.List;

public class MembersActivity extends AppCompatActivity {

    public static final String ROOM_SECRET = "roomSecret";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        String roomSecret = getIntent().getExtras().getString(ROOM_SECRET);

        applyColorTheme();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Room room = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getRoomBySecretName(roomSecret);

                List<Member> members = DiraRoomDatabase.getDatabase(getApplicationContext()).getMemberDao().getMembersByRoomSecret(room.getSecretName());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MembersAdapter membersAdapter = new MembersAdapter(MembersActivity.this, roomSecret);
                        membersAdapter.setMembers(members);

                        recyclerView.setAdapter(membersAdapter);
                    }
                });
            }
        });
        thread.start();

        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void applyColorTheme() {
        ColorTheme theme = AppTheme.getInstance().getColorTheme();

        ImageView button_back = findViewById(R.id.button_back);
        button_back.setColorFilter(theme.getAccentColor());
    }
}