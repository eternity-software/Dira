package com.diraapp.ui.adapters.messagetooltipread;

import android.graphics.Bitmap;

public class UserReadMessage {

    private String nickName;

    private String picturePath;

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
