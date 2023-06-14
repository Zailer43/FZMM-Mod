package fzmm.zailer.me.client.logic.headGenerator.model;

import fzmm.zailer.me.client.gui.headgenerator.category.HeadModelCategory;
import fzmm.zailer.me.client.gui.headgenerator.category.HeadPaintableCategory;
import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.headGenerator.model.parameters.*;
import fzmm.zailer.me.client.logic.headGenerator.model.steps.IModelStep;
import fzmm.zailer.me.utils.ImageUtils;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class HeadModelEntry extends AbstractHeadEntry implements IParametersEntry {

    private final List<IModelStep> steps;
    private final List<ResettableModelParameter<BufferedImage, String>> textures;
    private final List<? extends IModelParameter<Color>> colors;
    private final List<? extends IModelParameter<OffsetParameter>> offsets;
    private boolean isPaintable;
    private boolean isEditingSkinBody;
    private boolean isFirstResult;

    public HeadModelEntry() {
        this("", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public HeadModelEntry(String displayName, String key, List<IModelStep> steps,
                          List<ResettableModelParameter<BufferedImage, String>> textures,
                          List<? extends IModelParameter<Color>> colors,
                          List<? extends IModelParameter<OffsetParameter>> offsets) {
        super(displayName, key);
        this.steps = steps;
        this.textures = textures;
        this.colors = colors;
        this.offsets = offsets;
        this.isPaintable = false;
        this.isEditingSkinBody = false;
        this.isFirstResult = false;
    }

    @Override
    public BufferedImage getHeadSkin(BufferedImage baseSkin) {
        BufferedImage headSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

        Graphics2D destinationGraphics = headSkin.createGraphics();
        Color selectedColor = Color.WHITE;
        List<ResettableModelParameter<BufferedImage, String>> texturesCopy = new ArrayList<>(this.textures);
        List<IModelParameter<Color>> colorsCopy = new ArrayList<>(this.colors);
        List<IModelParameter<OffsetParameter>> offsetsCopy = new ArrayList<>(this.offsets);

        texturesCopy.add(new ResettableModelParameter<>("base_skin", baseSkin, null, false));
        texturesCopy.add(new ResettableModelParameter<>("destination_skin", headSkin, null, false));

        ModelData modelData = new ModelData(destinationGraphics, "destination_skin", texturesCopy, colorsCopy, offsetsCopy, baseSkin, selectedColor);

        for (var step : this.steps)
            step.apply(modelData);

        for (var offset : this.offsets)
            offset.value().ifPresent(OffsetParameter::reset);

        destinationGraphics.dispose();

        return headSkin;
    }

    @Override
    public String getCategoryId() {
        return this.isPaintable ? HeadPaintableCategory.CATEGORY_ID : HeadModelCategory.CATEGORY_ID;
    }

    @Override
    public boolean isEditingSkinBody() {
        return this.isEditingSkinBody;
    }

    public void isEditingSkinBody(boolean value) {
        this.isEditingSkinBody = value;
    }

    @Override
    public boolean isFirstResult() {
        return this.isFirstResult;
    }

    public void isFirstResult(boolean value) {
        this.isFirstResult = value;
    }

    public void isPaintable(boolean value) {
        this.isPaintable = value;
    }

    public boolean isPaintable() {
        return this.isPaintable;
    }

    @Override
    public List<? extends IModelParameter<Color>> getColors() {
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
    public List<ResettableModelParameter<BufferedImage, String>> getTextures() {
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
    public List<? extends IModelParameter<OffsetParameter>> getOffsets() {
        return this.offsets;
    }

    @Override
    public boolean hasParameters() {
        return this.getColors().stream().anyMatch(IModelParameter::isRequested)
                || this.getTextures().stream().anyMatch(IModelParameter::isRequested)
                || this.getOffsets().stream().anyMatch(IModelParameter::isRequested);
    }

    public void reset() {
        for (var textureParameter : this.textures) {
            BufferedImage texture;
            String defaultValue = textureParameter.getDefaultValue();
            if (defaultValue != null) {
                Identifier textureIdentifier = new Identifier(defaultValue);
                texture = ImageUtils.getBufferedImgFromIdentifier(textureIdentifier).orElseThrow(() -> new NoSuchElementException(defaultValue));
            } else {
                texture = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            }

            textureParameter.setValue(texture);
        }
    }
}
