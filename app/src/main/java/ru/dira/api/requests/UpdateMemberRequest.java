package ru.dira.api.requests;

import java.util.List;

public class UpdateMemberRequest extends Request {

    private String nickname, base64pic, id;

    private List<String> roomSecrets;
    private long updateTime;


    public UpdateMemberRequest(String nickname, String base64pic, List<String> roomSecrets, String id, long updateTime) {
        super(0, RequestType.UPDATE_MEMBER);
        this.nickname = nickname;
        this.base64pic = base64pic;
        this.roomSecrets = roomSecrets;
        this.id = id;
        this.updateTime = updateTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBase64pic() {
        return base64pic;
    }

    public void setBase64pic(String base64pic) {
        this.base64pic = base64pic;
    }

    public List<String> getRoomSecrets() {
        return roomSecrets;
    }

    public void setRoomSecrets(List<String> roomSecrets) {
        this.roomSecrets = roomSecrets;
    }
}
