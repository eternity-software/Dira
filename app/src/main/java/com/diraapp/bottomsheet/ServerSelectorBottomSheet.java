package com.diraapp.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.adapters.RoomServerAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ServerSelectorBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetListener bottomSheetListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_server_selector, container, true);

        RecyclerView recyclerView = v.findViewById(R.id.recycler_view);

        RoomServerAdapter roomServerAdapter = new RoomServerAdapter(getActivity());
        roomServerAdapter.setServerSelectorListener(new RoomServerAdapter.ServerSelectorListener() {
            @Override
            public void onServerSelect(String address) {
                bottomSheetListener.onServerSelected(address);
                dismiss();
            }
        });

        recyclerView.setAdapter(roomServerAdapter);


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
        void onServerSelected(String serverAddress);
    }

}

