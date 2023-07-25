package com.diraapp.ui.adapters;

import com.diraapp.db.entities.Member;

public class StatusMember {

    private Member member;

    private MemberStatus status;

    public StatusMember(Member member, MemberStatus status) {
        this.member = member;
        this.status = status;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }
}
