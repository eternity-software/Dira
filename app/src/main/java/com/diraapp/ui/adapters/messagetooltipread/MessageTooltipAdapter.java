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
import com.diraapp.ui.adapters.MembersAdapter;

import java.util.ArrayList;
import java.util.List;

public class MessageTooltipAdapter extends RecyclerView.Adapter<MessageTooltipAdapter.ViewHolder> {

    private Context context;

    private List<UserReadMessage> users = new ArrayList<>();

    private final LayoutInflater layoutInflater;

    public MessageTooltipAdapter(Context context, List<UserReadMessage> list) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        users = list;
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
        if (userReadMessage.getPicture() != null) {
            holder.userPictureView.setImageBitmap(userReadMessage.getPicture());
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nickNameView;
        ImageView userPictureView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nickNameView = itemView.findViewById(R.id.user_read_nickname);
            userPictureView = itemView.findViewById(R.id.user_read_picture);
        }
    }
}
