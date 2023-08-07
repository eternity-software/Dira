package com.diraapp.ui.adapters.messagetooltipread;

import android.graphics.Bitmap;

public class UserReadMessage {

    private String nickName;

    private Bitmap picture;

    public UserReadMessage(String nickName, Bitmap picture) {
        this.nickName = nickName;
        this.picture = picture;
    }

    public String getNickName() {
        return nickName;
    }

    public Bitmap getPicture() {
        return picture;
    }
}
