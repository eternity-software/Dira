package com.diraapp.api.processors;

import android.content.Context;
import android.graphics.Bitmap;

import com.diraapp.api.views.InviteRoom;
import com.diraapp.api.views.RoomMember;
import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.NewRoomUpdate;
import com.diraapp.api.updates.RoomUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.daos.MemberDao;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Message;
import com.diraapp.db.entities.Room;
import com.diraapp.exceptions.OldUpdateException;
import com.diraapp.storage.AppStorage;

public class RoomUpdatesProcessor {


    private final RoomDao roomDao;
    private final MemberDao memberDao;
    private final Context context;

    public RoomUpdatesProcessor(RoomDao roomDao, MemberDao memberDao, Context context) {
        this.roomDao = roomDao;
        this.memberDao = memberDao;
        this.context = context;
    }


    public void onNewRoom(NewRoomUpdate newRoomUpdate, String serverAddress) {

        InviteRoom inviteRoom = newRoomUpdate.getInviteRoom();

        Room oldRoom = roomDao.getRoomBySecretName(newRoomUpdate.getRoomSecret());
        if (oldRoom == null) {

            Room room = new Room(inviteRoom.getName(), System.currentTimeMillis(), inviteRoom.getSecretName(), serverAddress);

            if (inviteRoom.getBase64pic() != null) {
                Bitmap bitmap = AppStorage.getBitmapFromBase64(inviteRoom.getBase64pic());
                room.setImagePath(AppStorage.saveToInternalStorage(bitmap, room.getSecretName(), room.getSecretName(), context));
            }

            room.setLastUpdateId(0);

            for (RoomMember roomMember : inviteRoom.getMemberList()) {
                System.out.println("Memers invited");
                Member member = memberDao.getMemberByIdAndRoomSecret(roomMember.getId(), roomMember.getRoomSecret());

                boolean hasMemberInDatabase = true;
                if (member == null) {
                    hasMemberInDatabase = false;
                    String imagePath = null;
                    if (roomMember.getImageBase64() != null) {
                        imagePath = AppStorage.saveToInternalStorage(
                                AppStorage.getBitmapFromBase64(roomMember.getImageBase64()), room.getSecretName(), context);
                    }
                    System.out.println("Member: " + roomMember.getNickname());
                    member = new Member(roomMember.getId(), roomMember.getNickname(),
                            imagePath, roomMember.getRoomSecret(), roomMember.getLastTimeUpdated());
                }


                member.setLastTimeUpdated(roomMember.getLastTimeUpdated());
                member.setNickname(roomMember.getNickname());

                if (roomMember.getImageBase64() != null) {
                    Bitmap bitmap = AppStorage.getBitmapFromBase64(roomMember.getImageBase64());
                    String path = AppStorage.saveToInternalStorage(bitmap, member.getId() + "_" + roomMember.getRoomSecret(),
                            room.getSecretName(), context);
                    member.setImagePath(path);
                }

                if (!hasMemberInDatabase) {
                    memberDao.insertAll(member);
                } else {
                    memberDao.update(member);
                }

            }

            roomDao.insertAll(room);
        }
    }

    /**
     * Apply changes of room to local database
     * <p>
     * Argument can be represented only with
     * {@link com.diraapp.api.updates.NewMessageUpdate NewMessageUpdate} and {@link com.diraapp.api.updates.RoomUpdate RoomUpdate}
     *
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

                if (newMessage != null) {
                    DiraMessageDatabase.getDatabase(context).getMessageDao().insertAll(newMessage);
                }
                roomDao.update(room);
            } else {

                throw new OldUpdateException();
            }

        }
    }


    private void compareStartupTimes(Room room) {

        if (room.getTimeServerStartup() != UpdateProcessor.getInstance().getTimeServerStartup(room.getServerAddress())) {
            room.setLastUpdateId(0);
            room.setTimeServerStartup(UpdateProcessor.getInstance().getTimeServerStartup(room.getServerAddress()));
            roomDao.update(room);
        }
    }


}
