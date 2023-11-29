package com.diraapp.api.processors;

import com.diraapp.api.requests.encryption.SendIntermediateKey;
import com.diraapp.api.requests.encryption.SubmitKeyRequest;
import com.diraapp.api.updates.DhInitUpdate;
import com.diraapp.api.updates.KeyReceivedUpdate;
import com.diraapp.api.updates.RenewingCancelUpdate;
import com.diraapp.api.updates.RenewingConfirmUpdate;
import com.diraapp.api.views.BaseMember;
import com.diraapp.api.views.DhInfo;
import com.diraapp.api.views.DhKey;
import com.diraapp.db.daos.MemberDao;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Logger;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class DiraKeyProtocolProcessor {

    private final HashMap<String, DhInfo> roomMembers = new HashMap<>();
    private final HashMap<String, String> tempGeneratedKeys = new HashMap<>();
    private final RoomDao roomDao;

    private final MemberDao memberDao;
    private final CacheUtils cacheUtils;

    private final int number = 0;

    public DiraKeyProtocolProcessor(RoomDao roomDao, MemberDao memberDao, CacheUtils cacheUtils) {
        this.roomDao = roomDao;
        this.memberDao = memberDao;
        this.cacheUtils = cacheUtils;
    }

    public void onDiffieHellmanInit(DhInitUpdate dhInitUpdate) {
        Room room = roomDao.getRoomBySecretName(dhInitUpdate.getRoomSecret());
        if (room != null) {
            Random random = new SecureRandom();
            BigInteger mainBigint = new BigInteger(2048, random);
            BigInteger bigIntegerTwo = BigInteger.valueOf(2);
            room.setClientSecret(mainBigint.add(bigIntegerTwo).toString());
            roomDao.update(room);
            DhInfo dhInfo = new DhInfo(dhInitUpdate.getMemberList(), dhInitUpdate.getG(), dhInitUpdate.getP());

            roomMembers.put(dhInitUpdate.getRoomSecret(), dhInfo);

            Logger.logDebug(this.getClass().getSimpleName(), "DhInit ready");

            checkIfHasNewMember(dhInfo.getMemberList(), room.getSecretName());

            DhKey dhKey = new DhKey(dhInfo.getG(), 1, getNextToId(dhInfo.getMemberList(), cacheUtils.getString(CacheUtils.ID)).getId());
            sendToNextUser(dhKey, room.getSecretName(), room.getServerAddress());

        }
    }

    public void onIntermediateKey(KeyReceivedUpdate keyReceivedUpdate) {
        Room room = roomDao.getRoomBySecretName(keyReceivedUpdate.getRoomSecret());
        if (!keyReceivedUpdate.getDhKey().getRecipientMemberId().equals(cacheUtils.getString(CacheUtils.ID)))
            return;
        if (room != null) {
            DhInfo dhInfo = roomMembers.get(keyReceivedUpdate.getRoomSecret());

            if (dhInfo != null) {
                DhKey dhKey = keyReceivedUpdate.getDhKey();
                Logger.logDebug(this.getClass().getSimpleName(), "Received N=" + dhKey.getN());
                BigInteger G = new BigInteger(dhKey.getG());
                if (G.equals(BigInteger.ONE)) return;
                BigInteger P = new BigInteger(dhInfo.getP());
                BigInteger clientSecret = new BigInteger(room.getClientSecret());

                dhKey.setG(G.modPow(clientSecret, P).toString());
                if (dhKey.getN() == dhInfo.getMemberList().size()) {
                    tempGeneratedKeys.put(room.getSecretName(), dhKey.getG());
                    BaseMember baseMember = new BaseMember(cacheUtils.getString(CacheUtils.ID), cacheUtils.getString(CacheUtils.NICKNAME));
                    SubmitKeyRequest submitKeyRequest = new SubmitKeyRequest(room.getSecretName(), baseMember);
                    try {
                        UpdateProcessor.getInstance().sendRequest(submitKeyRequest, room.getServerAddress());

                        Logger.logDebug(this.getClass().getSimpleName(), "Key ready G=" + dhKey.getG());
                    } catch (UnablePerformRequestException e) {

                    }
                    return;
                } else if (dhKey.getN() > dhInfo.getMemberList().size()) {
                    return;
                }


                dhKey.setN(dhKey.getN() + 1);
                dhKey.setRecipientMemberId(getNextToId(dhInfo.getMemberList(), dhKey.getRecipientMemberId()).getId());
                sendToNextUser(dhKey, room.getSecretName(), room.getServerAddress());
                Logger.logDebug(this.getClass().getSimpleName(), "Key sent to N=" + dhKey.getN());
            }

        }
    }

    public void onKeyConfirmed(RenewingConfirmUpdate renewingConfirmUpdate) {
        Room room = roomDao.getRoomBySecretName(renewingConfirmUpdate.getRoomSecret());
        if (room != null) {
            room.setTimeEncryptionKeyUpdated(renewingConfirmUpdate.getTimeKeyConfirmed());
            room.setEncryptionKey(tempGeneratedKeys.get(renewingConfirmUpdate.getRoomSecret()));
            roomDao.update(room);
        }

    }

    public void onKeyCancel(RenewingCancelUpdate renewingCancelUpdate) {
        tempGeneratedKeys.remove(renewingCancelUpdate.getRoomSecret());
    }

    public BaseMember getNextToId(List<BaseMember> baseMembers, String id) {
        int index = 0;
        for (BaseMember baseMember : baseMembers) {
            if (baseMember.getId().equals(id)) {
                index = baseMembers.indexOf(baseMember) + 1;
            }
        }

        if (index == baseMembers.size()) {
            index = 0;
        }
        return baseMembers.get(index);
    }

    public void sendToNextUser(DhKey dhKey, String roomSecret, String serverAddress) {
        Logger.logDebug(this.getClass().getSimpleName(), "Key sent to id=" + dhKey.getRecipientMemberId());
        SendIntermediateKey sendIntermediateKey = new SendIntermediateKey(dhKey, roomSecret);
        try {
            UpdateProcessor.getInstance().sendRequest(sendIntermediateKey, serverAddress);
        } catch (UnablePerformRequestException e) {

        }
    }

    private void checkIfHasNewMember(List<BaseMember> baseMembers, String roomSecret) {
        long time = System.currentTimeMillis();
        int size = baseMembers.size();
        List<Member> realMembers = memberDao.getMembersByRoomSecret(roomSecret);
        HashSet<String> realMembersIds = new HashSet<>(size);

        for (Member member : realMembers) {
            realMembersIds.add(member.getId());
        }
        realMembersIds.add(cacheUtils.getString(CacheUtils.ID));

        ArrayList<Member> newMembers = new ArrayList<>(size);
        for (BaseMember baseMember : baseMembers) {
            Logger.logDebug(this.getClass().getSimpleName(), "Member (" + baseMember.getNickname() + " " + baseMember.getId());


            if (!realMembersIds.contains(baseMember.getId())) {
                newMembers.add(new Member(baseMember.getId(), baseMember.getNickname(),
                        null, roomSecret, time));
            }
        }

        if (newMembers.size() != 0) {
            for (Member newMember : newMembers) {
                memberDao.insertAll(newMember);
            }
        }
    }

}
