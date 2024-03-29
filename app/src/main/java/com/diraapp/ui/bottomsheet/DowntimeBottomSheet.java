package com.diraapp.ui.bottomsheet;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.diraapp.R;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.TimeConverter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DowntimeBottomSheet extends BottomSheetDialogFragment {


    private View view;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Пустой фон
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_downtime_alert, container, true);
        view = v;
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView timeText = v.findViewById(R.id.time_text);
        ImageView timeIcon = v.findViewById(R.id.time_icon);
        TextView timeSubtext = v.findViewById(R.id.time_subtext);

        long lastOnlineTimestamp = new CacheUtils(getContext()).getLong(CacheUtils.UPDATER_LAST_ACTIVE_TIME);

        if (System.currentTimeMillis() - lastOnlineTimestamp < 1000L * 60 * 60 * 6) {
            timeSubtext.setText(getString(R.string.downtime_default_info));
        } else {
            timeIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.red));
        }

        timeText.setText(getString(R.string.downtime_time).replace("%s", TimeConverter.getFormattedTimeAfterTimestamp(lastOnlineTimestamp, getContext())));

        v.findViewById(R.id.button_ignore).setOnClickListener(view -> dismiss());
        v.findViewById(R.id.check_permissions_button).setOnClickListener(view -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


}
