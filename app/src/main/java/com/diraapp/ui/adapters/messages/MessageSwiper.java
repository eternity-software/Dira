package com.diraapp.ui.adapters.messages;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.utils.Logger;

import java.util.ArrayList;

public class MessageSwiper extends ItemTouchHelper.SimpleCallback {

    private RecyclerView recyclerView;

    private ArrayList<MessageSwipingListener> listeners = new ArrayList<>();

    private Vibrator vibrator;

    public MessageSwiper(RecyclerView recyclerView) {
        super(0, ItemTouchHelper.LEFT);
        this.recyclerView = recyclerView;
        vibrator = (Vibrator) recyclerView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

        if (((ViewHolder) viewHolder).roomUpdatesLayout != null) return;

        //recyclerView.getAdapter().notifyItemChanged(position);

        MessageSwiper.this.notifyListeners(position);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 2.0f;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 0.1f;
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.1f;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        if (((ViewHolder) viewHolder).roomUpdatesLayout != null) return;
        dX = dX / 2;
        int maxOffset = -recyclerView.getWidth() / 5;
        if (dX < maxOffset) dX = maxOffset;
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void notifyListeners(int position) {
        for (MessageSwipingListener listener: listeners) {
            listener.onMessageSwiped(position);
        }
    }

    public void addListener(MessageSwipingListener listener) {
        listeners.add(listener);
    }

    public interface MessageSwipingListener {

        void onMessageSwiped(int position);
    }
}
