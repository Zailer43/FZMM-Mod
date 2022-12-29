package fzmm.zailer.me.client.logic.headGenerator;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class HeadGenerator {
    private final BufferedImage image;

    public HeadGenerator(@NotNull BufferedImage skin, boolean overlapHatLayer) {
        this.image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = this.image.createGraphics();
        g2d.drawImage(skin, 0, 0, 32, 16, 0, 0, 32, 16, null);
        if (overlapHatLayer)
            g2d.drawImage(skin, 0, 0, 32, 16, 32, 0, 64, 16, null);
        g2d.dispose();
    }

    public HeadGenerator addTexture(BufferedImage texture) {
        return this.addTexture(texture, true);
    }
    public HeadGenerator addTexture(BufferedImage texture, boolean hatLayer) {
        if (texture == null)
            return this;

        Graphics2D g2d = this.image.createGraphics();
        this.addLayer(g2d, texture, hatLayer);
        g2d.dispose();
        return this;
    }

    private void addLayer(Graphics2D finalImageGraphics, BufferedImage newLayer, boolean hatLayer) {
        int width = hatLayer ? 64 : 32;
        finalImageGraphics.drawImage(newLayer, 0, 0, width, 16, 0, 0, width, 16, null);
    }

    public BufferedImage getHeadTexture() {
        return this.image;
    }
}
