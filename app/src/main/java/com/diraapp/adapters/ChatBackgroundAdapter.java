package com.diraapp.adapters;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.appearance.AppTheme;
import com.diraapp.appearance.ChatBackground;
import com.diraapp.appearance.ColorTheme;

import java.util.ArrayList;
import java.util.List;

public class ChatBackgroundAdapter extends RecyclerView.Adapter<ChatBackgroundAdapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private final Activity context;

    private List<ChatBackground> list = new ArrayList<>();

    public ChatBackgroundAdapter(Activity context, List<ChatBackground> list) {
        this.context = context;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
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
        holder.imageView.setImageBitmap(background.getBitMap(context));

        if (background.equals(AppTheme.getInstance().getChatBackground())) {
            GradientDrawable drawable = (GradientDrawable) holder.layout.getBackground();
            drawable.setStroke(3, AppTheme.getInstance().getColorTheme().getAccentColor());
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (background.equals(AppTheme.getInstance().getChatBackground())) {
                    AppTheme.getInstance().clearBackground(context);
                }

                GradientDrawable drawable = (GradientDrawable) holder.layout.getBackground();
                drawable.setStroke(3, AppTheme.getInstance().getColorTheme().getAccentColor());

                int i = list.indexOf(AppTheme.getInstance().getChatBackground());
                notifyItemChanged(i);

                AppTheme.getInstance().setChatBackground(background, context);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
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