package fzmm.zailer.me.client.logic.headGenerator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelArea;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelPoint;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ModelCopyStep implements IModelStep {

    private final ModelPoint destination;
    private final ModelArea source;

    public ModelCopyStep(ModelPoint destination, ModelArea source) {
        this.destination = destination;
        this.source = source;
    }

    @Override
    public void apply(Graphics2D graphics, HashMap<String, BufferedImage> textures, AtomicReference<BufferedImage> selectedTexture) {
        int destinationX = this.destination.getXWithOffset();
        int destinationY = this.destination.getYWithOffset();
        int sourceX = this.source.getXWithOffset();
        int sourceY = this.source.getYWithOffset();

        graphics.drawImage(selectedTexture.get(),
                destinationX,
                destinationY,
                destinationX + this.source.width(),
                destinationY + this.source.height(),
                sourceX,
                sourceY,
                sourceX + this.source.width(),
                sourceY + this.source.height(),
                null
        );
    }

    public static ModelCopyStep parse(JsonObject jsonObject) {
        ModelArea source = ModelArea.parse(jsonObject.get("source").getAsJsonObject());
        ModelPoint destination = jsonObject.has("destination") ? ModelPoint.parse(jsonObject.get("destination").getAsJsonObject())
                : new ModelPoint(source.offset(), source.hatLayer(), source.getX(), source.getY());

        return new ModelCopyStep(destination, source);
    }
}
