package com.diraapp.ui.activities;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.requests.PingMembersRequest;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.ui.adapters.MembersAdapter;
import com.diraapp.utils.SliderActivity;

import java.util.List;

public class MembersActivity extends DiraActivity {

    public static final String ROOM_SECRET = "roomSecret";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        String roomSecret = getIntent().getExtras().getString(ROOM_SECRET);

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
                        try {
                            UpdateProcessor.getInstance().sendRequest(new PingMembersRequest(roomSecret), room.getServerAddress());
                        } catch (UnablePerformRequestException e) {
                            e.printStackTrace();
                        }

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

}