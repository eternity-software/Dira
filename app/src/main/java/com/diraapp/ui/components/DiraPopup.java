package com.diraapp.ui.components;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.diraapp.R;

public class DiraPopup {

    private final Activity context;
    private EditText input;
    private boolean cancellable = true;
    private AlertDialog alertDialog;


    public DiraPopup(Activity context) {
        this.context = context;

    }

    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }

    public EditText getInput() {
        return input;
    }

    private void showToast(String string) {
        if (context == null) return;
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void show(String title, String text, String inputHint, Drawable background, Runnable onClick) {
        LayoutInflater inflater = context.getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.popup_window, null);

        TextView titleView = dialoglayout.findViewById(R.id.text_title);
        TextView subtitleView = dialoglayout.findViewById(R.id.text_subtitle);
        TextView actionButton = dialoglayout.findViewById(R.id.button_action);
        TextView dismissButton = dialoglayout.findViewById(R.id.button_action_dismiss);
        ImageView imageView = dialoglayout.findViewById(R.id.image);
        input = dialoglayout.findViewById(R.id.edit_text);

        if (!cancellable) dismissButton.setVisibility(View.GONE);

        if (inputHint != null) {
            input.setHint(inputHint);
            input.setVisibility(View.VISIBLE);
        } else {
            input.setVisibility(View.GONE);
        }

        titleView.setText(title);
        subtitleView.setText(text);
        if (background == null) {
            imageView.setVisibility(View.GONE);
        }
        try {
            imageView.setImageDrawable(background);
        } catch (Exception e) {
            e.printStackTrace();
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);

        builder.setCancelable(cancellable);
        builder.setView(dialoglayout);
        alertDialog = builder.show();

        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClick != null) {
                    onClick.run();
                }
                alertDialog.dismiss();
            }
        });
    }

    public AlertDialog getAlertDialog() {
        return alertDialog;
    }

    public void show(String title, String text, int resId, Runnable on) {
        LayoutInflater inflater = context.getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.popup_window, null);

        TextView titleView = dialoglayout.findViewById(R.id.text_title);
        TextView subtitleView = dialoglayout.findViewById(R.id.text_subtitle);
        ImageView imageView = dialoglayout.findViewById(R.id.image);

        titleView.setText(title);
        subtitleView.setText(text);

        if (resId != -1) {
            imageView.setImageResource(resId);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
        builder.setCancelable(cancellable);
        builder.setView(dialoglayout);
        builder.show();
    }
}