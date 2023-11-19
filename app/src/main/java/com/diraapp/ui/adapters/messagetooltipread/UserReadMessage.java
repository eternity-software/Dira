package com.diraapp.ui.adapters.messagetooltipread;

public class UserReadMessage {

    private final String nickName;

    private final String picturePath;

    private final boolean isListened;

    public UserReadMessage(String nickName, String picture, boolean isListened) {
        this.nickName = nickName;
        this.picturePath = picture;
        this.isListened = isListened;
    }

    public String getNickName() {
        return nickName;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public boolean isListened() {
        return isListened;
    }
}
