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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class InvitationCodeBottomSheet extends BottomSheetDialogFragment {

    private final View.OnClickListener passiveButtonClick = null;
    private final View.OnClickListener activeButtonClick = null;
    private BottomSheetListener bottomSheetListener;

    private String code;
    private String roomName;
    private boolean isOfficialServer;
    private View view;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Пустой фон
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setOfficialServer(boolean officialServer) {
        isOfficialServer = officialServer;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_invitation, container, true);
        view = v;
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView codeView = v.findViewById(R.id.invitation_code_text);
        TextView serverAdress = v.findViewById(R.id.server_address);
        ImageView copyCodeButton = v.findViewById(R.id.copy_invitation_code);

        //codeTip.setText(getActivity().getString(R.string.invitation_code_tip).replace("%s", roomName));
        codeView.setText(code);

        if (isOfficialServer) {
            String link = UpdateProcessor.OFFICIAL_ADDRESS.split("//")[1].split(":")[0];
            link += "/join/";
            serverAdress.setText(link);
        } else {
            serverAdress.setText(R.string.unofficial_server);
            serverAdress.setTextColor(getResources().getColor(R.color.yellow));
        }

        copyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Invitation code", code);
                clipboard.setPrimaryClip(clip);
                dismiss();
            }
        });


        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            bottomSheetListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context
                    + " must implement BottomSheetListener");
        }
    }

    public interface BottomSheetListener {
    }

}

