package com.diraapp.ui.adapters.messagetooltipread;

public class UserReadMessage {

    private final String nickName;

    private final String picturePath;

    public UserReadMessage(String nickName, String picture) {
        this.nickName = nickName;
        this.picturePath = picture;
    }

    public String getNickName() {
        return nickName;
    }

    public String getPicturePath() {
        return picturePath;
    }
}
