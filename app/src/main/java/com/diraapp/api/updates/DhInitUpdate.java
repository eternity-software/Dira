package com.diraapp.api.updates;

import com.diraapp.api.views.BaseMember;

import java.util.List;

/**
 * Initialize Diffie-Hellman protocol
 */
public class DhInitUpdate extends Update {

    private List<BaseMember> memberList;

    private String G;
    private String P;


    public DhInitUpdate(List<BaseMember> memberList, String g, String p) {
        super(0, UpdateType.DIFFIE_HELLMAN_INIT_UPDATE);
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
