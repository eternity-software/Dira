package com.diraapp.api.updates;

public class RoomUpdate extends Update {

    private String base64Pic;
    private String name;

    public RoomUpdate(String base64Pic, String name) {
        super(0, UpdateType.ROOM_UPDATE);
        this.base64Pic = base64Pic;
        this.name = name;
    }

    public String getBase64Pic() {
        return base64Pic;
    }

    public void setBase64Pic(String base64Pic) {
        this.base64Pic = base64Pic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
