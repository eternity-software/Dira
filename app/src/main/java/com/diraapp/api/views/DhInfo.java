package com.diraapp.api.views;

import com.diraapp.api.views.BaseMember;

import java.util.List;

public class DhInfo {

    private List<BaseMember> memberList;

    private String G, P;

    public DhInfo(List<BaseMember> memberList, String g, String p) {
        this.memberList = memberList;
        G = g;
        P = p;
    }

    public List<BaseMember> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<BaseMember> memberList) {
        this.memberList = memberList;
    }

    public String getG() {
        return G;
    }

    public void setG(String g) {
        G = g;
    }

    public String getP() {
        return P;
    }

    public void setP(String p) {
        P = p;
    }
}
