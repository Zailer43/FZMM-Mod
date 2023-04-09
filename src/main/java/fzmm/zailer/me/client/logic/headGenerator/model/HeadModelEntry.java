package fzmm.zailer.me.client.logic.headGenerator.model;

import fzmm.zailer.me.client.gui.headgenerator.category.HeadModelCategory;
import fzmm.zailer.me.client.gui.headgenerator.category.HeadPaintableCategory;
import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.headGenerator.model.parameters.IParametersEntry;
import fzmm.zailer.me.client.logic.headGenerator.model.parameters.ModelParameter;
import fzmm.zailer.me.client.logic.headGenerator.model.steps.IModelStep;
import io.wispforest.owo.ui.core.Color;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class HeadModelEntry extends AbstractHeadEntry implements IParametersEntry {

    private final List<IModelStep> steps;
    private final List<ModelParameter<BufferedImage>> textures;
    private final List<ModelParameter<Color>> colors;
    private boolean isPaintable;

    public HeadModelEntry(String displayName, String key, List<IModelStep> steps, List<ModelParameter<BufferedImage>> textures, List<ModelParameter<Color>> colors) {
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
        List<ModelParameter<BufferedImage>> texturesCopy = new ArrayList<>(this.textures);
        List<ModelParameter<Color>> colorsCopy = new ArrayList<>(this.colors);

        texturesCopy.add(new ModelParameter<>("base_skin", baseSkin, false));
        texturesCopy.add(new ModelParameter<>("destination_skin", headSkin, false));

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

    @Override
    public String getCategoryId() {
        return this.isPaintable ? HeadPaintableCategory.CATEGORY_ID : HeadModelCategory.CATEGORY_ID;
    }

    public void isPaintable(boolean value) {
        this.isPaintable = value;
    }

    public boolean isPaintable() {
        return this.isPaintable;
    }

    @Override
    public List<ModelParameter<Color>> getColors() {
        return this.colors;
    }

    @Override
    public void putColor(String key, Color color) {
        for (var colorEntry : this.colors) {
            if (colorEntry.id().equals(key)) {
                colorEntry.setValue(color);
                return;
            }
        }
    }

    @Override
    public List<ModelParameter<BufferedImage>> getTextures() {
        return this.textures;
    }

    public void putTexture(String key, BufferedImage texture) {
        for (var textureEntry : this.textures) {
            if (textureEntry.id().equals(key)) {
                textureEntry.setValue(texture);
                return;
            }
        }
    }

    @Override
    public boolean hasParameters() {
        return this.getColors().stream().anyMatch(ModelParameter::isRequested) || this.getTextures().stream().anyMatch(ModelParameter::isRequested);
    }
}
