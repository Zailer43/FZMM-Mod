package fzmm.zailer.me.client.logic.headGenerator.model;

import fzmm.zailer.me.client.gui.headgenerator.HeadGenerationMethod;
import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.headGenerator.model.steps.IModelStep;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class HeadModelEntry extends AbstractHeadEntry {

    private final List<IModelStep> steps;
    private final HashMap<String, BufferedImage> textures;

    public HeadModelEntry(String displayName, String key, List<IModelStep> steps, HashMap<String, BufferedImage> textures) {
        super(displayName, key);
        this.steps = steps;
        this.textures = textures;
    }

    @Override
    public BufferedImage getHeadSkin(BufferedImage baseSkin, boolean overlapHatLayer) {
        BufferedImage headSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = headSkin.createGraphics();
        AtomicReference<BufferedImage> selectedTexture = new AtomicReference<>(baseSkin);
        HashMap<String, BufferedImage> texturesCopy = new HashMap<>(this.textures);

        texturesCopy.put("base_skin", baseSkin);

        for (var step : this.steps)
            step.apply(graphics, texturesCopy, selectedTexture);

        graphics.dispose();

        return headSkin;
    }

    @Override
    public boolean canOverlap() {
        return false;
    }

    @Override
    public HeadGenerationMethod getGenerationMethod() {
        return HeadGenerationMethod.MODEL;
    }
}
