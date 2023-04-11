package fzmm.zailer.me.client.logic.headGenerator;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class TextureOverlap {
    private final BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

    public TextureOverlap() {
    }

    public TextureOverlap(@NotNull BufferedImage skin, boolean overlapHatLayer) {
        this.addTexture(skin, overlapHatLayer);
    }

    public TextureOverlap removeHatLayer() {
        Graphics2D g2d = this.image.createGraphics();

        g2d.setBackground(new Color(0, 0, 0, 0));
        g2d.clearRect(32, 0, 64, 16);

        g2d.dispose();
        return this;
    }


    public TextureOverlap addTexture(BufferedImage texture, boolean overlapHatLayer) {
        if (texture == null)
            return this;

        Graphics2D g2d = this.image.createGraphics();
        this.addLayer(g2d, texture, overlapHatLayer);
        g2d.dispose();
        return this;
    }

    private void addLayer(Graphics2D finalImageGraphics, BufferedImage newLayer, boolean overlapHatLayer) {
        finalImageGraphics.drawImage(newLayer, 0, 0, 32, 16, 0, 0, 32, 16, null);

        if (overlapHatLayer) {
            finalImageGraphics.drawImage(newLayer, 0, 0, 32, 16, 32, 0, 64, 16, null);
        } else {
            finalImageGraphics.drawImage(newLayer, 32, 0, 64, 16, 32, 0, 64, 16, null);
        }

        if (newLayer.getHeight() == 64)
            this.addBody(finalImageGraphics, newLayer);
    }

    private void addBody(Graphics2D finalImageGraphics, BufferedImage newLayer) {
        if (newLayer.getHeight() == 64)
            finalImageGraphics.drawImage(newLayer, 0, 16, 64, 64, 0, 16, 64, 64, null);
    }

    public BufferedImage getHeadTexture() {
        return this.image;
    }
}
