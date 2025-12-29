package fzmm.zailer.me.client.logic.mineskin.model;

public enum MSVisibility {
    PUBLIC("public"),
    UNLISTED("unlisted"),
    PRIVATE("private");

    private final String value;

    MSVisibility(String value) {
        this.value = value;
    }

    public static MSVisibility parse(String value) {
        for (MSVisibility status : values()) {
            if (status.value.equals(value)) return status;
        }

        return UNLISTED;
    }

    public String value() {
        return this.value;
    }
}
