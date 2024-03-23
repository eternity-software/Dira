package com.diraapp.ui.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.ui.appearance.AppTheme;
import com.diraapp.ui.appearance.ChatBackground;

import java.util.ArrayList;
import java.util.List;

public class ChatBackgroundAdapter extends RecyclerView.Adapter<ChatBackgroundAdapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private final Activity context;
    private final SelectorListener listener;
    private List<ChatBackground> list = new ArrayList<>();

    public ChatBackgroundAdapter(Activity context, List<ChatBackground> list, SelectorListener listener) {
        this.context = context;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatBackgroundAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatBackgroundAdapter.ViewHolder(layoutInflater.inflate(R.layout.background_choose, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatBackgroundAdapter.ViewHolder holder, int position) {
        ChatBackground background = list.get(position);

        holder.name.setText(background.getName());
        holder.imageView.setImageDrawable(background.getDrawable(context));

        if (background.equals(AppTheme.getInstance(context).getChatBackground())) {
            holder.layout.getBackground().setTint(Theme.getColor(context, R.color.accent));
        } else {
            holder.layout.getBackground().setTint(Theme.getColor(context, R.color.gray));
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.layout.getBackground().setTint(
                        Theme.getColor(context, R.color.accent));

                int i = list.indexOf(AppTheme.getInstance(context).getChatBackground());
                notifyItemChanged(i);

                AppTheme.getInstance(context).setChatBackground(background, context);

                listener.onSelectorClicked(background);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface SelectorListener {

        void onSelectorClicked(ChatBackground background);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView imageView;

        LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.name = itemView.findViewById(R.id.appearance_name);
            this.imageView = itemView.findViewById(R.id.appearance_image);
            this.layout = (LinearLayout) itemView;
        }
    }
}