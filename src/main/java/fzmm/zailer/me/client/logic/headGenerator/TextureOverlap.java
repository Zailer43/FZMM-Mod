package fzmm.zailer.me.client.logic.headGenerator;

import fzmm.zailer.me.client.gui.headgenerator.options.ISkinPreEdit;
import fzmm.zailer.me.client.gui.headgenerator.options.SkinPreEditOption;
import fzmm.zailer.me.utils.SkinPart;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public final class TextureOverlap {
    private final BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

    public TextureOverlap(@NotNull BufferedImage skin) {
        this.addTexture(skin);
    }

    public TextureOverlap overlap(boolean isEditingSkinBody) {
        ISkinPreEdit overlapPreEdit = SkinPreEditOption.OVERLAP.getPreEdit();
        overlapPreEdit.execute(this.image, this.image, List.of(SkinPart.HEAD));

        if (isEditingSkinBody)
            overlapPreEdit.execute(this.image, this.image, SkinPart.BODY_PARTS);
        return this;
    }


    public TextureOverlap addTexture(BufferedImage texture) {
        if (texture == null)
            return this;

        Graphics2D g2d = this.image.createGraphics();
        g2d.drawImage(texture, 0, 0, null);
        g2d.dispose();
        return this;
    }

    public BufferedImage getHeadTexture() {
        return this.image;
    }
}
