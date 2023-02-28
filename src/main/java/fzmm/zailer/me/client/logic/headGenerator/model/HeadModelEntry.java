package fzmm.zailer.me.client.logic.headGenerator.model;

import fzmm.zailer.me.client.gui.headgenerator.HeadGenerationMethod;
import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.headGenerator.model.steps.IModelStep;
import io.wispforest.owo.ui.core.Color;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class HeadModelEntry extends AbstractHeadEntry implements IPaintableEntry {

    private final List<IModelStep> steps;
    private final HashMap<String, BufferedImage> textures;
    private final HashMap<String, Color> colors;
    private boolean isPaintable;

    public HeadModelEntry(String displayName, String key, List<IModelStep> steps, HashMap<String, BufferedImage> textures, HashMap<String, Color> colors) {
        super(displayName, key);
        this.steps = steps;
        this.textures = textures;
        this.colors = colors;
        this.isPaintable = false;
    }

    @Override
    public BufferedImage getHeadSkin(BufferedImage baseSkin, boolean overlapHatLayer) {
        BufferedImage headSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = headSkin.createGraphics();
        AtomicReference<BufferedImage> selectedTexture = new AtomicReference<>(baseSkin);
        AtomicReference<Color> selectedColor = new AtomicReference<>(Color.WHITE);
        HashMap<String, BufferedImage> texturesCopy = new HashMap<>(this.textures);
        HashMap<String, Color> colorsCopy = new HashMap<>(this.colors);

        texturesCopy.put("base_skin", baseSkin);
        texturesCopy.put("destination_skin", headSkin);
        if (this.isPaintable() && !colorsCopy.containsKey("selected_color")) {
            colorsCopy.put("selected_color", Color.WHITE);
        }

        ModelData modelData = new ModelData(graphics, texturesCopy, colorsCopy, selectedTexture, selectedColor);

        for (var step : this.steps)
            step.apply(modelData);

        graphics.dispose();

        return headSkin;
    }

    @Override
    public boolean canOverlap() {
        return false;
    }

    public void isPaintable(boolean value) {
        this.isPaintable = value;
    }

    @Override
    public boolean isPaintable() {
        return this.isPaintable;
    }

    @Override
    public void putColor(String key, Color color) {
        this.colors.put(key, color);
    }

    @Override
    public Color getColor(String key) {
        Color color = this.colors.get(key);
        return color == null ? Color.WHITE : color;
    }

    @Override
    public HeadGenerationMethod getGenerationMethod() {
        return HeadGenerationMethod.MODEL;
    }
}
