package com.diraapp.ui.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.databinding.ActivityRoomServersBinding;
import com.diraapp.storage.AppStorage;
import com.diraapp.ui.adapters.RoomServerAdapter;
import com.diraapp.ui.components.DiraPopup;
import com.diraapp.utils.SliderActivity;

import java.util.ArrayList;

public class RoomServersActivity extends AppCompatActivity {

    private ActivityRoomServersBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomServersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonAdd.setOnClickListener((View v) -> {
            String address = binding.serverAddress.getText().toString();

            if (address.length() < 4) return;

            if (!address.startsWith("wss://")) {
                DiraPopup diraPopup = new DiraPopup(this);
                diraPopup.setCancellable(false);
                diraPopup.show(getString(R.string.room_servers_popup_wrong_server_title),
                        getString(R.string.room_servers_popup_wrong_server_text), null,
                        null, null);
                return;
            }

            binding.serverAddress.setText("");
            ArrayList<String> servers = AppStorage.getServerList(this);
            if (!servers.contains(address)) {
                servers.add(address);
                AppStorage.saveServerList(servers, this);
                RoomServerAdapter roomServerAdapter = new RoomServerAdapter(this);
                binding.recyclerView.setAdapter(roomServerAdapter);

                Thread thread = new Thread(() -> {
                    UpdateProcessor.getInstance().reconnectSockets();
                });
                thread.start();
            }


        });

        binding.buttonBack.setOnClickListener((View v) -> {
            onBackPressed();
        });

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        RoomServerAdapter roomServerAdapter = new RoomServerAdapter(this);
        binding.recyclerView.setAdapter(roomServerAdapter);
    }


}