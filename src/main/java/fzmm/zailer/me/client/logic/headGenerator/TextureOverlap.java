package fzmm.zailer.me.client.logic.headGenerator;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class TextureOverlap {
    private final BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

    public TextureOverlap() {
    }

    public TextureOverlap(@NotNull BufferedImage skin, boolean overlapHatLayer) {
        Graphics2D g2d = this.image.createGraphics();

        this.addTexture(skin, !overlapHatLayer);
        if (!overlapHatLayer)
            g2d.drawImage(skin, 0, 0, 32, 16, 32, 0, 64, 16, null);

        g2d.dispose();
    }

    public TextureOverlap addTexture(BufferedImage texture) {
        return this.addTexture(texture, true);
    }

    public TextureOverlap addTexture(BufferedImage texture, boolean hatLayer) {
        if (texture == null)
            return this;

        Graphics2D g2d = this.image.createGraphics();
        this.addLayer(g2d, texture, hatLayer);
        g2d.dispose();
        return this;
    }

    private void addLayer(Graphics2D finalImageGraphics, BufferedImage newLayer, boolean hatLayer) {
        boolean newLayerHasBody = newLayer.getHeight() == 64;

        if (hatLayer && newLayerHasBody) {
            finalImageGraphics.drawImage(newLayer, 0, 0, 64, 64, 0, 0, 64, 64, null);
            return;
        }

        int width = hatLayer ? 64 : 32;
        finalImageGraphics.drawImage(newLayer, 0, 0, width, 16, 0, 0, width, 16, null);

        if (newLayerHasBody)
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
