package com.diraapp.ui.bottomsheet.roomoptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.diraapp.R;
import com.diraapp.databinding.BottomSheetRoomSettingsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import java.io.File;

public class RoomOptionsBottomSheet extends BottomSheetDialogFragment {

    private final boolean showInviteButton;
    private final String roomName;
    private final String imagePath;
    private final boolean isNotificationsEnabled;
    private BottomSheetRoomSettingsBinding binding;
    private RoomOptionsBottomSheetListener listener;

    public RoomOptionsBottomSheet(boolean showInviteButton, String roomName, String imagePath, boolean isNotificationsEnabled, RoomOptionsBottomSheetListener listener) {
        this.showInviteButton = showInviteButton;
        this.roomName = roomName;
        this.imagePath = imagePath;
        this.isNotificationsEnabled = isNotificationsEnabled;
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetRoomSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        binding.roomName.setText(roomName);
        if (imagePath != null) {
            Picasso.get().load(new File(imagePath))
                    .into(binding.roomImage);
        }
        initButtons();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
        listener = null;
    }

    public void initButtons() {
        if (!isNotificationsEnabled) {
            binding.notificationButtonIcon.setImageDrawable(ContextCompat.getDrawable(
                    binding.getRoot().getContext(), R.drawable.ic_notification_enabled));
            binding.notificationButtonText.setText(
                    binding.getRoot().getContext().getString(R.string.enable_notification));
        }

        binding.notificationButton.setOnClickListener((View v) -> {
            listener.onNotificationButtonClicked();
            dismiss();
        });

        binding.encryptionButton.setOnClickListener((View v) -> {
            listener.onEncryptionButtonClicked();
            dismiss();
        });

        binding.deleteRoom.setOnClickListener((View v) -> {
            listener.onRoomDeleteClicked();
            dismiss();
        });

        if (!showInviteButton) {
            binding.inviteMemberButton.setVisibility(View.GONE);
            return;
        }

        binding.inviteMemberButton.setOnClickListener((View v) -> {
            listener.onInviteButtonClicked();
            dismiss();
        });
    }
}
