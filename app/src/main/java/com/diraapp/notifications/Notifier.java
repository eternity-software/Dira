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
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.images.ImagesWorker;
import com.diraapp.ui.activities.NavigationActivity;
import com.diraapp.ui.activities.room.RoomActivity;
import com.diraapp.utils.CacheUtils;

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

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, DIRA_ID)
                        .setSmallIcon(R.drawable.logo_white)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);


        CacheUtils cacheUtils = new CacheUtils(context);
        if (message.hasAuthor()) {
            if (message.getAuthorId().equals(cacheUtils.getString(CacheUtils.ID))) return;
        }

        String text;
        if (message.hasAuthor()) {
            text = message.getAuthorNickname() + ": " + message.getMessageTextPreview(context);
        } else {
            text = message.getCustomClientData().getText(context);
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

            Intent notificationIntent = new Intent(context, NavigationActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            RoomActivity.putRoomExtrasInIntent(notificationIntent, room.getSecretName(), room.getName());


            PendingIntent intent;

            intent = PendingIntent.getActivity(context, 0,
                    notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

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
            // or other logo_white behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
