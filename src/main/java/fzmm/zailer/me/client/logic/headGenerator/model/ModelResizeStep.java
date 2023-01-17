package fzmm.zailer.me.client.logic.headGenerator.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.playerStatue.statueHeadSkin.SkinPart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ModelResizeStep implements IModelStep {

    private final ModelArea destination;
    private final ModelArea source;
    private final boolean overlapSourceHat;

    public ModelResizeStep(ModelArea destination, ModelArea source, boolean overlapSourceHat) {
        this.destination = destination;
        this.source = source;
        this.overlapSourceHat = overlapSourceHat;
    }

    @Override
    public void apply(Graphics2D graphics, HashMap<String, BufferedImage> textures, AtomicReference<BufferedImage> selectedTexture) {
        if (this.overlapSourceHat) {
            this.apply(graphics, selectedTexture, false);
            this.apply(graphics, selectedTexture, true);
        } else {
            this.apply(graphics, selectedTexture, this.destination.hatLayer());
        }
    }

    private void apply(Graphics2D graphics, AtomicReference<BufferedImage> selectedTexture, boolean sourceHatLayer) {
        SkinPart destinationOffset = this.destination.offset();
        SkinPart sourceOffset = this.source.offset();
        int destinationX = (this.destination.hatLayer() ? destinationOffset.hatX() : destinationOffset.x()) + this.destination.x();
        int destinationY = (this.destination.hatLayer() ? destinationOffset.hatY() : destinationOffset.y()) + this.destination.y();
        int sourceX = (sourceHatLayer ? sourceOffset.hatX() : sourceOffset.x()) + this.source.x();
        int sourceY = (sourceHatLayer ? sourceOffset.hatY() : sourceOffset.y()) + this.source.y();

        graphics.drawImage(selectedTexture.get(),
                destinationX,
                destinationY,
                destinationX + this.destination.width(),
                destinationY + this.destination.height(),
                sourceX,
                sourceY,
                sourceX + this.source.width(),
                sourceY + this.source.height(),
                null
        );
    }

    public static ModelResizeStep parse(JsonObject jsonObject) {
        ModelArea destination = ModelArea.parse(jsonObject.get("destination").getAsJsonObject());
        ModelArea source = ModelArea.parse(jsonObject.get("source").getAsJsonObject());
        boolean overlapSourceHat = jsonObject.has("overlap_source_hat") && jsonObject.get("overlap_source_hat").getAsBoolean();

        return new ModelResizeStep(destination, source, overlapSourceHat);
    }
}
