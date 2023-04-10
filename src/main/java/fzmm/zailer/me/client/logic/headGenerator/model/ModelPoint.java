package fzmm.zailer.me.client.logic.headGenerator.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.utils.SkinPart;

public class ModelPoint {
    private final SkinPart offset;
    private final boolean hatLayer;
    private final byte x;
    private final byte y;

    public ModelPoint(SkinPart offset, boolean hatLayer, int x, int y) {
        this.offset = offset;
        this.hatLayer = hatLayer;
        this.x = (byte) x;
        this.y = (byte) y;
    }

    public static ModelPoint parse(JsonObject areaObject) {
        String offsetString = areaObject.get("offset").getAsString();
        SkinPart offset = SkinPart.fromString(offsetString);
        boolean hat_layer = areaObject.get("hat_layer").getAsBoolean();
        int x = areaObject.get("x").getAsInt();
        int y = areaObject.get("y").getAsInt();
        return new ModelPoint(offset, hat_layer, x, y);
    }

    public int getXWithOffset() {
        return this.getXWithOffset(this.hatLayer);
    }

    public int getXWithOffset(boolean hatLayer) {
        return (hatLayer ? this.offset.hatX() : this.offset.x()) + this.x;
    }

    public int getYWithOffset() {
        return this.getYWithOffset(this.hatLayer);
    }

    public int getYWithOffset(boolean hatLayer) {
        return (hatLayer ? this.offset.hatY() : this.offset.y()) + this.y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public SkinPart offset() {
        return offset;
    }

    public boolean hatLayer() {
        return hatLayer;
    }
}
