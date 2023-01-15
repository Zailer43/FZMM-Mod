package fzmm.zailer.me.client.logic.headGenerator.model;

import fzmm.zailer.me.client.gui.headgenerator.HeadGenerationMethod;
import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import fzmm.zailer.me.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class HeadModelEntry extends AbstractHeadEntry {

    private final boolean convertInSteveModel;
    private final List<IModelStep> steps;

    public HeadModelEntry(String displayName, String key, List<IModelStep> steps, boolean convertInSteveModel) {
        super(displayName, key);
        this.steps = steps;
        this.convertInSteveModel = convertInSteveModel;
    }

    @Override
    public BufferedImage getHeadSkin(BufferedImage baseSkin, boolean overlapHatLayer) {
        BufferedImage headSkin = new BufferedImage(baseSkin.getWidth(), baseSkin.getHeight(), BufferedImage.TYPE_INT_ARGB);

        if (this.convertInSteveModel && ImageUtils.isAlexModel(1, baseSkin))
            baseSkin = ImageUtils.convertInSteveModel(baseSkin, 1);

        Graphics2D graphics = headSkin.createGraphics();

        for (var step : this.steps)
            step.apply(graphics, baseSkin);

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
