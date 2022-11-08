package fzmm.zailer.me.client.logic.headGenerator;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class HeadGenerator {
    private final BufferedImage image;

    public HeadGenerator(@NotNull BufferedImage image) {
        this.image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        this.addTexture(image, false);
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
