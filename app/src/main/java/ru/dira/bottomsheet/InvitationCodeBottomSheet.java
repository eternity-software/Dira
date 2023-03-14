package ru.dira.bottomsheet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ru.dira.R;

public class InvitationCodeBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetListener bottomSheetListener;

    // Присваемывый контент
    private String code;
    private String roomName;

    // Нажатия на кнопки
    private View.OnClickListener passiveButtonClick = null;
    private View.OnClickListener activeButtonClick = null;

    private View view;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Пустой фон
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }


    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);

        // Кастомная анимация
        // getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();

        // Получение поведения
        final LinearLayout bottomSheet = dialog.findViewById(R.id.bottom_sheet);

        // Ловим COLLAPSED и не даём промежуточному положению существовать, а так же убираем слайд при невозможности отменить



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

    // Отрисовка элементов
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.invitation_bottom_sheet, container, true);
        view = v;

        TextView codeView = v.findViewById(R.id.invitation_code_text);
        TextView codeTip = v.findViewById(R.id.invitation_code_tip);
        ImageView copyCodeButton = v.findViewById(R.id.copy_invitation_code);

        codeTip.setText(getActivity().getString(R.string.invitation_code_tip).replace("%s", roomName));
        codeView.setText(code);

        copyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Invitation code", code);
                clipboard.setPrimaryClip(clip);
            }
        });


        return v;
    }

    public interface BottomSheetListener {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            bottomSheetListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetListener");
        }
    }


}

