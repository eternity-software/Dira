package com.diraapp.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.components.DiraPopup;
import com.diraapp.storage.AppStorage;
import com.diraapp.updates.UpdateProcessor;

import java.util.ArrayList;

public class RoomServerAdapter extends RecyclerView.Adapter<RoomServerAdapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private final Activity context;
    private ArrayList<String> servers = new ArrayList<>();

    private ServerSelectorListener serverSelectorListener;

    public RoomServerAdapter(Activity context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        servers = AppStorage.getServerList(context);
    }

    public void setServerSelectorListener(ServerSelectorListener serverSelectorListener) {
        this.serverSelectorListener = serverSelectorListener;
    }

    @NonNull
    @Override
    public RoomServerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RoomServerAdapter.ViewHolder(layoutInflater.inflate(R.layout.room_server_element, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RoomServerAdapter.ViewHolder holder,
                                 @SuppressLint("RecyclerView") int position) {
        String serverAddress = servers.get(position);

        if (serverAddress.equals(UpdateProcessor.OFFICIAL_ADDRESS)) {
            holder.deleteButton.setVisibility(View.GONE);
            holder.serverAddressView.setText(context.getString(R.string.room_servers_official));
            holder.serverAddressView.setTextColor(context.getResources().getColor(R.color.accent));
        } else {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.serverAddressView.setTextColor(context.getResources().getColor(R.color.white));
            holder.serverAddressView.setText(serverAddress);
        }

        if (serverSelectorListener != null) {
            holder.itemView.setOnClickListener((View v) -> {
                serverSelectorListener.onServerSelect(serverAddress);
            });
        }

        holder.deleteButton.setOnClickListener((View v) -> {
            DiraPopup diraPopup = new DiraPopup(context);
            diraPopup.show(context.getString(R.string.room_servers_popup_delete_server_title),
                    context.getString(R.string.room_servers_popup_delete_server_text), null,
                    null, new Runnable() {
                        @Override
                        public void run() {
                            servers.remove(serverAddress);
                            AppStorage.saveServerList(servers, context);
                            notifyItemRemoved(position);
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    public interface ServerSelectorListener {
        void onServerSelect(String address);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView serverAddressView;
        ImageView deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deleteButton = itemView.findViewById(R.id.button_delete);
            serverAddressView = itemView.findViewById(R.id.server_address);
        }
    }
}
