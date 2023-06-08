package fzmm.zailer.me.client.gui.headgenerator.options;

import fzmm.zailer.me.utils.SkinPart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public interface ISkinPreEdit {

    default BufferedImage execute(BufferedImage result, BufferedImage skin, List<SkinPart> skinParts) {
        Graphics2D graphics = result.createGraphics();

        for (SkinPart skinPart : skinParts)
            this.execute(graphics, skin, skinPart);

        graphics.dispose();

        return result;
    }

    void execute(Graphics2D graphics, BufferedImage skin, SkinPart skinPart);
}
