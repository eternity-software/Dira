package com.diraapp.api.views;

public class DhKey {
    private String G;
    private int n;
    private String recipientMemberId;

    public DhKey(String g, int n, String recipientMemberId) {
        G = g;
        this.n = n;
        this.recipientMemberId = recipientMemberId;
    }

    public String getG() {
        return G;
    }

    public void setG(String g) {
        G = g;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public String getRecipientMemberId() {
        return recipientMemberId;
    }

    public void setRecipientMemberId(String recipientMemberId) {
        this.recipientMemberId = recipientMemberId;
    }
}
