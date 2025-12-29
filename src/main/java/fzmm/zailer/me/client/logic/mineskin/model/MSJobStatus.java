package fzmm.zailer.me.client.logic.mineskin.model;

public enum MSJobStatus {
    UNKNOWN("unknown", true),
    WAITING("waiting", false),
    PROCESSING("processing", false),
    ACTIVE("active", false),
    FAILED("failed", true),
    COMPLETED("completed", true);

    private final String value;
    private final boolean isDone;

    MSJobStatus(String value, boolean isDone) {
        this.value = value;
        this.isDone = isDone;
    }

    public static MSJobStatus parse(String value) {
        for (MSJobStatus status : values()) {
            if (status.value.equals(value)) return status;
        }

        return UNKNOWN;
    }

    public boolean isDone() {
        return this.isDone;
    }
}
