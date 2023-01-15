package fzmm.zailer.me.client.logic.headGenerator.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.playerStatue.statueHeadSkin.SkinPart;

public record ModelArea(SkinPart offset, boolean hatLayer, int x, int y, int width, int height) {

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

}
