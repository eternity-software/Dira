package com.diraapp.ui.activities.createroom;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.diraapp.databinding.ActivityCreateRoomBinding;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.bottomsheet.ServerSelectorBottomSheet;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.SliderActivity;

public class CreateRoomActivity extends DiraActivity implements CreateRoomContract.View, ServerSelectorBottomSheet.BottomSheetListener {

    private ActivityCreateRoomBinding binding;

    private CreateRoomContract.Presenter presenter;

    private CacheUtils cacheUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cacheUtils = new CacheUtils(getApplicationContext());

        CreateRoomContract.Model model = new CreateRoomModel(DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao());
        presenter = new CreateRoomPresenter(this, model);
        initHandlers();
        attachSlider();
    }

    private void initHandlers() {
        binding.roomServer.setOnClickListener((View v) -> {
            selectServer();
        });
        binding.copySecretCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onCopyButtonClick((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE));
            }
        });

        binding.buttonCreateNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onCreateButtonClick();
            }
        });

        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void selectServer() {
        ServerSelectorBottomSheet serverSelectorBottomSheet = new ServerSelectorBottomSheet();

        serverSelectorBottomSheet.show(getSupportFragmentManager(), "Server selector  bottom sheet");
    }

    @Override
    public void attachSlider() {
        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);
    }

    @Override
    public void setSecretCodeText(String secretCode) {
        binding.secretCodeText.setText(secretCode.substring(0, 8) + "***");
    }

    @Override
    public void showToast(String text) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show());
    }

    @Override
    public String getRoomName() {
        return binding.roomNameEditText.getText().toString();
    }

    @Override
    public String getWelcomeMessage() {
        return binding.welcomeMessageEditText.getText().toString();
    }

    @Override
    public String getAuthorName() {
        return cacheUtils.getString(CacheUtils.NICKNAME);
    }

    @Override
    public String getAuthorId() {
        return cacheUtils.getString(CacheUtils.ID);
    }

    @Override
    public void onServerSelected(String serverAddress) {
        binding.roomServer.setText(serverAddress);
        presenter.setServer(serverAddress);
    }
}