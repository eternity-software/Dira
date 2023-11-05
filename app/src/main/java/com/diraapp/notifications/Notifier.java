package com.diraapp.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.diraapp.R;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.customclientdata.KeyGenerateStartClientData;
import com.diraapp.db.entities.messages.customclientdata.KeyGeneratedClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomJoinClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameAndIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameChangeClientData;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.images.ImagesWorker;
import com.diraapp.ui.activities.RoomSelectorActivity;
import com.diraapp.ui.activities.room.RoomActivity;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.StringFormatter;

public class Notifier {

    public static String DIRA_ID = "Dira";

    private static int notificationId = 1;

    public static void notifyMessage(Message message, Context context) {
        Room room = DiraRoomDatabase.getDatabase(context).getRoomDao().
                getRoomBySecretName(message.getRoomSecret());
        notifyMessage(message, room, context);
    }

    public static void notifyMessage(Message message, Room room, Context context) {
        createNotificationChannel(context);

        // Need to check if ClientMessage!

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, DIRA_ID)
                        .setSmallIcon(R.drawable.notification)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        String text = "";
        boolean hasMessageText = false;
        if (message.getText() != null) {
            hasMessageText = message.getText().length() != 0;
        }

        if (message.getCustomClientData() == null) {
            CacheUtils cacheUtils = new CacheUtils(context);
            if (message.getAuthorId().equals(cacheUtils.getString(CacheUtils.ID))) return;

            if (hasMessageText) {
                text = message.getAuthorNickname() + ": " + message.getText();
            } else if (message.getAttachments().size() > 0) {
                String attachmentText = message.getAttachmentText(context);

                text = message.getAuthorNickname() + ": " + attachmentText;
            }

        } else if (message.getCustomClientData() instanceof RoomJoinClientData) {
            text = context.getString(R.string.room_update_new_member).replace("%s", ((RoomJoinClientData)
                    message.getCustomClientData()).getNewNickName());
        } else if (message.getCustomClientData() instanceof RoomIconChangeClientData) {
            text = context.getString(R.string.room_update_picture_change);
        } else if (message.getCustomClientData() instanceof RoomNameChangeClientData) {
            text = context.getString(R.string.room_update_name_change);
        } else if (message.getCustomClientData() instanceof RoomNameAndIconChangeClientData) {
            text = context.getString(R.string.room_update_name_and_picture_change);
        } else if (message.getCustomClientData() instanceof KeyGenerateStartClientData) {
            text = context.getString(R.string.key_generate_start);
        } else if (message.getCustomClientData() instanceof KeyGeneratedClientData) {
            text = ((KeyGeneratedClientData) message.getCustomClientData()).getClientDataText(context);
        }

        if (!text.equals("")) {
            builder.setContentText(text);

            builder.setContentTitle(room.getName());
        }

        if (room != null) {
            if (!room.isNotificationsEnabled()) return;

            Bitmap userPicture;
            if (room.getImagePath() == null) {
//                userPicture = ((BitmapDrawable) ContextCompat.
//                        getDrawable(context, R.drawable.placeholder)).getBitmap();
                userPicture = BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder);
            } else {
                userPicture = AppStorage.getBitmapFromPath(room.getImagePath());
            }

            Bitmap bitmap = ImagesWorker.getCircleCroppedBitmap(userPicture, 256, 256);
            if (bitmap != null) {
                builder.setLargeIcon(bitmap);
            }

            Intent notificationIntent = new Intent(context, RoomSelectorActivity.class);
            RoomActivity.putRoomExtrasInIntent(notificationIntent, room.getSecretName(), room.getName());

            PendingIntent intent;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent = PendingIntent.getActivity(context, 0,
                        notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                intent = PendingIntent.getActivity(context, 0,
                        notificationIntent, 0);
            }

            builder.setContentIntent(intent);
        }

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(notificationId, builder.build());
        notificationId++;
    }


    public static void cancelAllNotifications(Context context) {
        NotificationManagerCompat.from(context).cancelAll();
    }

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Dira";
            String description = "";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(DIRA_ID, name, importance);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
