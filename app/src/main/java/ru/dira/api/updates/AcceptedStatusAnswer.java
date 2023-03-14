package ru.dira.api.updates;

public class AcceptedStatusAnswer extends Update{

    private boolean isAccepted;

    public AcceptedStatusAnswer(long updateId, boolean isAccepted) {
        super(updateId, UpdateType.ACCEPTED_STATUS);
        this.isAccepted = isAccepted;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }
}
