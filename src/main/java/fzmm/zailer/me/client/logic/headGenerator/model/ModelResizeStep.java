package fzmm.zailer.me.client.logic.headGenerator.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.playerStatue.statueHeadSkin.SkinPart;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ModelResizeStep implements IModelStep {

    private final ModelArea destination;
    private final ModelArea source;

    public ModelResizeStep(ModelArea destination, ModelArea source) {
        this.destination = destination;
        this.source = source;
    }

    @Override
    public void apply(Graphics2D graphics, BufferedImage baseSkin) {
        SkinPart destinationOffset = this.destination.offset();
        SkinPart sourceOffset = this.source.offset();
        int destinationX = (this.destination.hatLayer() ? destinationOffset.hatX() : destinationOffset.x()) + this.destination.x();
        int destinationY = (this.destination.hatLayer() ? destinationOffset.hatY() : destinationOffset.y()) + this.destination.y();
        int sourceX = (this.source.hatLayer() ? sourceOffset.hatX() : sourceOffset.x()) + this.source.x();
        int sourceY = (this.source.hatLayer() ? sourceOffset.hatY() : sourceOffset.y()) + this.source.y();

        graphics.drawImage(baseSkin,
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

    public static ModelResizeStep parse(JsonObject areaObject) {
        ModelArea destination = ModelArea.parse(areaObject.get("destination").getAsJsonObject());
        ModelArea source = ModelArea.parse(areaObject.get("source").getAsJsonObject());

        return new ModelResizeStep(destination, source);
    }
}
