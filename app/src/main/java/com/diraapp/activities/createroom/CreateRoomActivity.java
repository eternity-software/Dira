package com.diraapp.activities.createroom;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.diraapp.databinding.ActivityCreateRoomBinding;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.SliderActivity;

public class CreateRoomActivity extends AppCompatActivity implements CreateRoomContract.View {

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
    }


    private void initHandlers() {
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
}