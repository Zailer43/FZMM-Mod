package fzmm.zailer.me.client.logic.headGenerator.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.headGenerator.model.parameters.ModelParameter;
import fzmm.zailer.me.client.logic.headGenerator.model.parameters.OffsetParameter;
import fzmm.zailer.me.utils.SkinPart;

import java.util.List;

public class ModelArea extends ModelPoint {
    protected final byte width;
    protected final byte height;

    public ModelArea(SkinPart offset, boolean hatLayer, int x, int y, int width, int height) {
        super(offset, hatLayer, x, y);
        this.width = (byte) width;
        this.height = (byte) height;
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

    public ModelArea copyWithOffset(List<ModelParameter<OffsetParameter>> offsets) {
        ModelArea copy = new ModelArea(this.offset, this.hatLayer, this.x, this.y, this.width, this.height);
        for (var offset : offsets) {
            offset.value().ifPresent(offsetParameter -> {
                if (!offsetParameter.enabled())
                    return;

                if (offsetParameter.isXAxis())
                    copy.x += offsetParameter.value();
                else
                    copy.y += offsetParameter.value();
            });
        }
        return copy;
    }
}
