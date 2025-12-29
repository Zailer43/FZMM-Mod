package fzmm.zailer.me.client.logic.mineskin.model;

public enum MSVariant {
    CLASSIC("classic"),
    SLIM("slim"),
    UNKNOWN("unknown");

    private final String value;

    MSVariant(String value) {
        this.value = value;
    }

    public static MSVariant parse(String value) {
        for (MSVariant status : values()) {
            if (status.value.equals(value)) return status;
        }

        return UNKNOWN;
    }

    public String value() {
        return this.value;
    }
}
