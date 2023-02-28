package fzmm.zailer.me.client.logic.headGenerator.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.utils.SkinPart;

public class ModelArea extends ModelPoint {
    private final int width;
    private final int height;

    public ModelArea(SkinPart offset, boolean hatLayer, int x, int y, int width, int height) {
        super(offset, hatLayer, x, y);
        this.width = width;
        this.height = height;
    }

    public static ModelArea parse(JsonObject areaObject) {
        String offsetString = areaObject.get("offset").getAsString();
        SkinPart offset = SkinPart.fromString(offsetString);
        boolean hat_layer = areaObject.get("hat_layer").getAsBoolean();
        int x = areaObject.get("x").getAsInt();
        int y = areaObject.get("y").getAsInt();
        int width = areaObject.get("width").getAsInt();
        int height = areaObject.get("height").getAsInt();
        return new ModelArea(offset, hat_layer, x, y, width, height);
    }
    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }
}
