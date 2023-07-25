package com.diraapp.ui.bottomsheet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.requests.PingMembersRequest;
import com.diraapp.db.entities.Room;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RoomEncryptionBottomSheet extends BottomSheetDialogFragment {

    private Room room;

    public RoomEncryptionBottomSheet(Room room) {
        this.room = room;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Пустой фон
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_encryption, container, true);


        TextView keyView = v.findViewById(R.id.encryption_key_text);

        String key = room.getEncryptionKey();

        if(!key.equals(""))
        {
            keyView.setText("..********" + key.substring(key.length() - 4));
        }

        v.findViewById(R.id.button_update_key).setOnClickListener((v2) -> {




            RoomKeyRenewingBottomSheet roomKeyRenewingBottomSheet = new RoomKeyRenewingBottomSheet(room);
            roomKeyRenewingBottomSheet.show(getParentFragmentManager(), "");

        });

        v.findViewById(R.id.copy_encryption_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Encryption key", room.getEncryptionKey());
                clipboard.setPrimaryClip(clip);
                dismiss();
            }
        });


        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


}