package com.diraapp.ui.activities.fragments.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.diraapp.BuildConfig;
import com.diraapp.databinding.FragmentAccountBinding;
import com.diraapp.device.PerformanceTester;
import com.diraapp.storage.AppStorage;
import com.diraapp.ui.activities.PersonalityActivity;
import com.diraapp.ui.activities.SettingsActivity;
import com.diraapp.utils.CacheUtils;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    private CacheUtils cacheUtils;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AccountViewModel accountViewModel =
                new ViewModelProvider(this).get(AccountViewModel.class);

        cacheUtils = new CacheUtils(getContext());
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        updateData();

        binding.buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PersonalityActivity.class);
            startActivity(intent);
        });

        binding.buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
    }


    public void updateData()
    {
        ImageView imageView = binding.userPicture;
        String picPath = cacheUtils.getString(CacheUtils.PICTURE);
        String nickname = cacheUtils.getString(CacheUtils.NICKNAME);
        String id = cacheUtils.getString(CacheUtils.ID);
        if (picPath != null) imageView.setImageBitmap(AppStorage.getBitmapFromPath(picPath));

        binding.notImpl.setText("Not implemented in " + BuildConfig.VERSION_NAME);
        binding.nicknameText.setText(nickname);

        if(id != null)
        {
            if(id.length() > 4) id = id.substring(0, 4);
        }

        binding.idText.setText(id + "*****" );
        binding.version.setText(BuildConfig.VERSION_NAME + ", code " + BuildConfig.VERSION_CODE + ", device class " +
                PerformanceTester.measureDevicePerformanceClass(getContext()));
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}