package fzmm.zailer.me.client.logic.headGenerator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelArea;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ModelCopyStep implements IModelStep {

    private final ModelArea destination;
    private final ModelArea source;
    private final boolean addHatLayer;
    private final boolean overlapSourceHat;
    private final int degrees;
    private final boolean mirrorHorizontal;
    private final boolean mirrorVertical;

    public ModelCopyStep(ModelArea destination, ModelArea source, boolean addHatLayer, boolean overlapSourceHat, int degrees, boolean mirrorHorizontal, boolean mirrorVertical) {
        this.destination = destination;
        this.source = source;
        this.addHatLayer = addHatLayer;
        this.overlapSourceHat = overlapSourceHat;
        this.degrees = degrees;
        this.mirrorHorizontal = mirrorHorizontal;
        this.mirrorVertical = mirrorVertical;
    }

    @Override
    public void apply(Graphics2D graphics, HashMap<String, BufferedImage> textures, AtomicReference<BufferedImage> selectedTexture) {
        if (this.addHatLayer) {
            this.apply(graphics, selectedTexture, false, false);
            this.apply(graphics, selectedTexture, true, true);
        } else if (this.overlapSourceHat) {
            this.apply(graphics, selectedTexture, false, this.destination.hatLayer());
            this.apply(graphics, selectedTexture, true, this.destination.hatLayer());
        } else {
            this.apply(graphics, selectedTexture, this.source.hatLayer(), this.destination.hatLayer());
        }
    }

    private void apply(Graphics2D graphics, AtomicReference<BufferedImage> selectedTexture, boolean sourceHatLayer, boolean destinationHatLayer) {
        int destinationX = this.destination.getXWithOffset(destinationHatLayer);
        int destinationY = this.destination.getYWithOffset(destinationHatLayer);
        int sourceX = this.source.getXWithOffset(sourceHatLayer);
        int sourceY = this.source.getYWithOffset(sourceHatLayer);

        AffineTransform transform = graphics.getTransform();
        transform.setToRotation(
                Math.toRadians(this.degrees),
                (this.source.width() / 2f) + destinationX,
                (this.source.height() / 2f) + destinationY
        );
        graphics.setTransform(transform);

        int destinationX2 = destinationX + this.destination.width();
        if (this.mirrorHorizontal) {
            int aux = destinationX;
            destinationX = destinationX2;
            destinationX2 = aux;
        }

        int destinationY2 = destinationY + this.destination.height();
        if (this.mirrorVertical) {
            int aux = destinationY;
            destinationY = destinationY2;
            destinationY2 = aux;
        }


        graphics.drawImage(selectedTexture.get(),
                destinationX,
                destinationY,
                destinationX2,
                destinationY2,
                sourceX,
                sourceY,
                sourceX + this.source.width(),
                sourceY + this.source.height(),
                null
        );
    }

    public static ModelCopyStep parse(JsonObject jsonObject) {
        ModelArea source = ModelArea.parse(jsonObject.get("source").getAsJsonObject());
        ModelArea destination = jsonObject.has("destination") ?
                ModelArea.parse(jsonObject.get("destination").getAsJsonObject())
                : new ModelArea(source.offset(), source.hatLayer(), source.getX(), source.getY(), source.width(), source.height());

        boolean addHatLayer = jsonObject.has("add_hat_layer") && jsonObject.get("add_hat_layer").getAsBoolean();
        boolean overlapSourceHat = jsonObject.has("overlap_source_hat") && jsonObject.get("overlap_source_hat").getAsBoolean();
        int degrees = jsonObject.has("degrees") ? jsonObject.get("degrees").getAsInt() : 0;
        boolean mirrorHorizontal = jsonObject.has("mirror_horizontal") && jsonObject.get("mirror_horizontal").getAsBoolean();
        boolean mirrorVertical = jsonObject.has("mirror_vertical") && jsonObject.get("mirror_vertical").getAsBoolean();

        return new ModelCopyStep(destination, source, addHatLayer, overlapSourceHat, degrees, mirrorHorizontal, mirrorVertical);
    }
}
