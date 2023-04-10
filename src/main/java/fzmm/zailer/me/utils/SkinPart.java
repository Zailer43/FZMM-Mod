package fzmm.zailer.me.utils;

import fzmm.zailer.me.utils.position.PosI;

public record SkinPart(byte x, byte y, byte hatX, byte hatY) {

    // https://imgur.com/3LlJdua
    public static final SkinPart RIGHT_LEG = new SkinPart((byte) 16, (byte) 48, (byte) 0, (byte) 48);
    public static final SkinPart RIGHT_ARM = new SkinPart((byte) 32, (byte) 48, (byte) 48, (byte) 48);
    public static final SkinPart LEFT_LEG = new SkinPart((byte) 0, (byte) 16, (byte) 0, (byte) 32);
    public static final SkinPart LEFT_ARM = new SkinPart((byte) 40, (byte) 16, (byte) 40, (byte) 32);
    public static final SkinPart BODY = new SkinPart((byte) 16, (byte) 16, (byte) 16, (byte) 32);
    public static final SkinPart HEAD = new SkinPart((byte) 0, (byte) 0, (byte) 32, (byte) 0);

    public PosI getNormalLayer() {
        return new PosI(this.x, this.y);
    }

    public PosI getHatLayer() {
        return new PosI(this.hatX, this.hatY);
    }

    public static SkinPart fromString(String value) {
        return switch (value.toUpperCase()) {
            case "RIGHT_LEG" -> RIGHT_LEG;
            case "RIGHT_ARM" -> RIGHT_ARM;
            case "LEFT_LEG" -> LEFT_LEG;
            case "LEFT_ARM" -> LEFT_ARM;
            case "BODY" -> BODY;
            default -> HEAD;
        };
    }
}
