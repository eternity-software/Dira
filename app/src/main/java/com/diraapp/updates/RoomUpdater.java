package com.diraapp.updates;

import android.content.Context;
import android.graphics.Bitmap;

import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.RoomUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.Message;
import com.diraapp.db.entities.Room;
import com.diraapp.exceptions.OldUpdateException;
import com.diraapp.storage.AppStorage;

public class RoomUpdater {


    private RoomDao roomDao;
    private Context context;

    public RoomUpdater(RoomDao roomDao, Context context) {
        this.roomDao = roomDao;
        this.context = context;
    }

    /**
     * Apply changes of room to local database
     *
     * Argument can be represented only with
     * {@link com.diraapp.api.updates.NewMessageUpdate NewMessageUpdate} and {@link com.diraapp.api.updates.RoomUpdate RoomUpdate}
     * @param update
     */
    public void updateRoom(Update update) throws OldUpdateException {
        Message newMessage = null;

        String roomSecret = update.getRoomSecret();

        if (update instanceof NewMessageUpdate) {
            newMessage = ((NewMessageUpdate) update).getMessage();
            roomSecret = ((NewMessageUpdate) update).getMessage().getRoomSecret();
        }

        Room room = roomDao.getRoomBySecretName(roomSecret);
        if (room != null) {


            compareStartupTimes(room);
            if (room.getLastUpdateId() < update.getUpdateId()) {
                room.setLastUpdateId(update.getUpdateId());

                if (newMessage != null) {
                    room.setLastMessageId(newMessage.getId());
                    room.setLastUpdatedTime(newMessage.getTime());
                    room.setUpdatedRead(false);
                }

                if (update instanceof RoomUpdate) {
                    room.setName(((RoomUpdate) update).getName());
                    if (((RoomUpdate) update).getBase64Pic() != null) {
                        Bitmap bitmap = AppStorage.getBitmapFromBase64(((RoomUpdate) update).getBase64Pic());
                        String path = AppStorage.saveToInternalStorage(bitmap, room.getSecretName(), room.getSecretName(), context);
                        room.setImagePath(path);
                    }
                }

                if(newMessage != null)
                {
                    DiraMessageDatabase.getDatabase(context).getMessageDao().insertAll(newMessage);
                }
                roomDao.update(room);
            } else {

                throw new OldUpdateException();
            }

        }
    }



    private void compareStartupTimes(Room room) {

        // TODO: startup time depend on room server
        if (room.getTimeServerStartup() != UpdateProcessor.getInstance().getTimeServerStartup()) {
            room.setLastUpdateId(0);
            room.setTimeServerStartup(UpdateProcessor.getInstance().getTimeServerStartup());
            roomDao.update(room);
        }
    }


}
