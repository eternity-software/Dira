package com.diraapp.ui.adapters.messagetooltipread;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageTooltipAdapter extends RecyclerView.Adapter<MessageTooltipAdapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private final Context context;
    private final boolean isListenable;
    private List<UserReadMessage> users = new ArrayList<>();

    public MessageTooltipAdapter(Context context, List<UserReadMessage> list, boolean isListenable) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        users = list;
        this.isListenable = isListenable;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.user_read_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserReadMessage userReadMessage = users.get(position);

        holder.nickNameView.setText(userReadMessage.getNickName());

        holder.bindListenIndicator(userReadMessage, isListenable);

        if (userReadMessage.getPicturePath() != null) {
            Picasso.get().load(new File(userReadMessage.getPicturePath())).into(holder.userPictureView);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nickNameView;
        ImageView userPictureView;

        LinearLayout listenedIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nickNameView = itemView.findViewById(R.id.user_read_nickname);
            userPictureView = itemView.findViewById(R.id.user_read_picture);
            listenedIndicator = itemView.findViewById(R.id.listened_indicator);
        }

        public void bindListenIndicator(UserReadMessage userReadMessage, boolean isListenable) {
            if (!isListenable) {
                this.listenedIndicator.setVisibility(View.INVISIBLE);
                return;
            }

            if (userReadMessage.isListened()) {
                this.listenedIndicator.setVisibility(View.INVISIBLE);
            } else {
                this.listenedIndicator.setVisibility(View.VISIBLE);
            }
        }
    }
}
