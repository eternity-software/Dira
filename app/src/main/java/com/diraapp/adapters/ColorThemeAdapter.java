package com.diraapp.adapters;

import android.app.Activity;
import android.content.res.ColorStateList;
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
import com.diraapp.appearance.ColorTheme;

import java.util.ArrayList;
import java.util.List;

public class ColorThemeAdapter extends RecyclerView.Adapter<ColorThemeAdapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private final Activity context;

    private List<ColorTheme> list = new ArrayList<>();

    private SelectorListener listener;

    public ColorThemeAdapter(Activity context, List<ColorTheme> list) {
        this.context = context;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ColorThemeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.color_scheme, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ColorThemeAdapter.ViewHolder holder, int position) {
        ColorTheme theme = list.get(position);

        holder.name.setText(theme.getName());
        holder.imageView.setBackgroundTintList(ColorStateList.valueOf(theme.getPreviewColor()));

        if (theme.equals(AppTheme.getInstance().getColorTheme())) {
            holder.layout.getBackground().setTint(AppTheme.getInstance().
                    getColorTheme().getAccentColor());
        } else {
            holder.layout.getBackground().setTint(context.getResources().getColor(R.color.gray));
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.layout.setBackgroundTintList(ColorStateList.valueOf(theme.getPreviewColor()));

                int i = list.indexOf(AppTheme.getInstance().getColorTheme());
                notifyItemChanged(i);

                AppTheme.getInstance().setColorTheme(theme, context);

                listener.onSelectorClicked();
            }
        });
    }

    public void setListener(SelectorListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface SelectorListener {

        void onSelectorClicked();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView imageView;

        LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.name = itemView.findViewById(R.id.appearance_name);
            this.imageView = itemView.findViewById(R.id.appearance_color);
            this.layout = (LinearLayout) itemView;
        }
    }
}
