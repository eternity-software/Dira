package ru.dira.api.updates;

public class MemberUpdate extends Update {

    private String nickname, base64pic, id;
    private long updateTime;

    public MemberUpdate(String nickname, String base64pic,  String id, long updateTime) {
        super(0, UpdateType.MEMBER_UPDATE);
        this.nickname = nickname;
        this.base64pic = base64pic;
        this.id = id;
        this.updateTime = updateTime;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
