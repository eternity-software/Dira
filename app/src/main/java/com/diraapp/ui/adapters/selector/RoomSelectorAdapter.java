package com.diraapp.ui.adapters.selector;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.utils.CacheUtils;

import java.util.ArrayList;
import java.util.List;

public class RoomSelectorAdapter extends RecyclerView.Adapter<SelectorViewHolder> {


    private final LayoutInflater layoutInflater;
    private final Activity context;
    private final SelectorAdapterContract contract;
    private final CacheUtils cacheUtils;
    private List<Room> roomList = new ArrayList<>();


    public RoomSelectorAdapter(Activity context, SelectorAdapterContract contract) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.contract = contract;

        cacheUtils = new CacheUtils(context);
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    @NonNull
    @Override
    public SelectorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SelectorViewHolder(layoutInflater.inflate(R.layout.room_element, parent, false), contract);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectorViewHolder holder, int position) {
        Room room = roomList.get(position);

        holder.onBind(room);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

}
