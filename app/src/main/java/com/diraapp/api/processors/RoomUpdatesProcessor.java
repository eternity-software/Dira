package com.diraapp.api.processors;

import android.content.Context;
import android.graphics.Bitmap;

import com.diraapp.api.updates.MemberUpdate;
import com.diraapp.api.updates.MessageReadUpdate;
import com.diraapp.api.views.InviteRoom;
import com.diraapp.api.views.RoomMember;
import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.NewRoomUpdate;
import com.diraapp.api.updates.RoomUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.daos.MemberDao;
import com.diraapp.db.daos.MessageDao;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.MessageReading;
import com.diraapp.exceptions.OldUpdateException;
import com.diraapp.storage.AppStorage;
import com.diraapp.utils.CacheUtils;

public class RoomUpdatesProcessor {


    private final RoomDao roomDao;
    private final MemberDao memberDao;
    private final MessageDao messageDao;
    private final Context context;

    public RoomUpdatesProcessor(RoomDao roomDao, MemberDao memberDao, MessageDao messageDao, Context context) {
        this.roomDao = roomDao;
        this.memberDao = memberDao;
        this.messageDao = messageDao;
        this.context = context;
    }


    public void onNewRoom(NewRoomUpdate newRoomUpdate, String serverAddress) {

        InviteRoom inviteRoom = newRoomUpdate.getInviteRoom();

        Room oldRoom = roomDao.getRoomBySecretName(newRoomUpdate.getRoomSecret());
        if (oldRoom == null) {

            Room room = new Room(inviteRoom.getName(), System.currentTimeMillis(), inviteRoom.getSecretName(), serverAddress, true);

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

                if (update instanceof RoomUpdate) {
                    String oldName = room.getName();
                    String newName = ((RoomUpdate) update).getName();

                    String path = null;

                    if (!oldName.equals(newName)) {
                        room.setName(newName);
                    }
                    if (((RoomUpdate) update).getBase64Pic() != null) {
                        Bitmap bitmap = AppStorage.getBitmapFromBase64(((RoomUpdate) update).getBase64Pic());
                        path = AppStorage.saveToInternalStorage(bitmap, room.getSecretName(), room.getSecretName(), context);
                        room.setImagePath(path);
                    }

                    if (!oldName.equals(newName) && path != null) {
                        newMessage = UpdateProcessor.getInstance().getClientMessageProcessor().
                                notifyRoomMessageAndIconChange((RoomUpdate) update, oldName, path, room);
                    } else if (!oldName.equals(newName)) {
                        newMessage = UpdateProcessor.getInstance().getClientMessageProcessor()
                                .notifyRoomNameChange((RoomUpdate) update, oldName, room);
                    } else if (path != null) {
                        newMessage = UpdateProcessor.getInstance().getClientMessageProcessor()
                                .notifyRoomIconChange((RoomUpdate) update, path, room);
                    }
                } else if (update instanceof MemberUpdate) {
                    newMessage = updateMember((MemberUpdate) update);
                } else if (update instanceof MessageReadUpdate) {
                    updateMessageReading((MessageReadUpdate) update);
                }

                if (newMessage != null) {
                    room.setLastMessageId(newMessage.getId());
                    room.setLastUpdatedTime(newMessage.getTime());
                    room.setUpdatedRead(false);
                    newMessage.setRead(false);
                    messageDao.insertAll(newMessage);
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

    /**
     * Apply changes of room member to local database
     *
     * @param memberUpdate
     */
    private Message updateMember(MemberUpdate memberUpdate) {
        if (memberUpdate.getId().equals(new CacheUtils(context).getString(CacheUtils.ID)))
            return null;

        Member member = memberDao.getMemberByIdAndRoomSecret(memberUpdate.getId(), memberUpdate.getRoomSecret());

        boolean hasMemberInDatabase = true;

        boolean newMember = false;

        if (member == null) {
            hasMemberInDatabase = false;
            member = new Member(memberUpdate.getId(), memberUpdate.getNickname(),
                    null, memberUpdate.getRoomSecret(), memberUpdate.getUpdateTime());

            // I think there we can observe for new user join room
            newMember = true;
        }

        member.setLastTimeUpdated(memberUpdate.getUpdateTime());
        member.setNickname(memberUpdate.getNickname());
        String path = null;

        if (memberUpdate.getBase64pic() != null) {
            Bitmap bitmap = AppStorage.getBitmapFromBase64(memberUpdate.getBase64pic());
            path = AppStorage.saveToInternalStorage(bitmap,
                    member.getId() + "_" + memberUpdate.getRoomSecret(), memberUpdate.getRoomSecret(), context);
            member.setImagePath(path);
        }

        if (hasMemberInDatabase) {
            memberDao.update(member);
        } else {
            memberDao.insertAll(member);
        }

        if (newMember) {
            return UpdateProcessor.getInstance().getClientMessageProcessor()
                    .notifyMemberAdded(memberUpdate, path);
        }
        return null;
    }

    private void updateMessageReading(MessageReadUpdate update) {
        if (update.getUserId().equals(new CacheUtils(context).getString(CacheUtils.ID))) return;

        Message message = messageDao.getMessageById(update.getMessageId());

        MessageReading messageReading = new MessageReading(update.getUserId(), update.getReadTime());

        if (message.getMessageReadingList().contains(messageReading)) return;

        UpdateProcessor.getInstance().notifyUpdateListeners(update);

        message.getMessageReadingList().add(messageReading);
        messageDao.update(message);
    }

}
