package com.diraapp.api.requests;

import com.diraapp.api.views.RoomInfo;

import java.util.ArrayList;

public class GetUpdatesRequest extends Request {

    private ArrayList<RoomInfo> roomInfoList = new ArrayList<>();

    public GetUpdatesRequest() {
        super(0, RequestType.GET_UPDATES);
    }

    public ArrayList<RoomInfo> getRoomInfoList() {
        return roomInfoList;
    }

    public void setRoomInfoList(ArrayList<RoomInfo> roomInfoList) {
        this.roomInfoList = roomInfoList;
    }
}
