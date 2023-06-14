package fzmm.zailer.me.client.logic.headGenerator.model;

import fzmm.zailer.me.client.logic.headGenerator.model.parameters.IModelParameter;
import fzmm.zailer.me.client.logic.headGenerator.model.parameters.OffsetParameter;
import fzmm.zailer.me.client.logic.headGenerator.model.parameters.ResettableModelParameter;
import io.wispforest.owo.ui.core.Color;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

public final class ModelData {
    private Graphics2D destinationGraphics;
    private String destinationId;
    private final List<ResettableModelParameter<BufferedImage, String>> textures;
    private final List<IModelParameter<Color>> colors;
    private final List<IModelParameter<OffsetParameter>> offsets;
    private BufferedImage selectedTexture;
    private Color selectedColor;

    public ModelData(Graphics2D destinationGraphics, String destinationId, List<ResettableModelParameter<BufferedImage, String>> textures,
                     List<IModelParameter<Color>> colors, List<IModelParameter<OffsetParameter>> offsets,
                     BufferedImage selectedTexture, Color selectedColor) {
        this.destinationGraphics = destinationGraphics;
        this.destinationId = destinationId;
        this.textures = textures;
        this.colors = colors;
        this.offsets = offsets;
        this.selectedTexture = selectedTexture;
        this.selectedColor = selectedColor;
    }

    public Color getColor(String key) {
        for (var colorEntry : this.colors) {
            if (colorEntry.id().equals(key))
                return colorEntry.value().orElse(Color.WHITE);
        }
        return Color.WHITE;
    }


    public Optional<BufferedImage> getTexture(String key) {
        for (var textureEntry : this.textures) {
            if (textureEntry.id().equals(key))
                return textureEntry.value();
        }
        return Optional.empty();
    }

    public Graphics2D destinationGraphics() {
        return this.destinationGraphics;
    }

    public String destinationId() {
        return this.destinationId;
    }

    public List<ResettableModelParameter<BufferedImage, String>> textures() {
        return this.textures;
    }

    public List<IModelParameter<Color>> colors() {
        return this.colors;
    }

    public List<IModelParameter<OffsetParameter>> offsets() {
        return this.offsets;
    }

    public BufferedImage selectedTexture() {
        return this.selectedTexture;
    }

    public Color selectedColor() {
        return this.selectedColor;
    }

    public void destinationGraphics(Graphics2D destinationGraphics) {
        this.destinationGraphics = destinationGraphics;
    }

    public void destinationId(String id) {
        this.destinationId = id;
    }

    public void selectedTexture(BufferedImage texture) {
        this.selectedTexture = texture;
    }

    public void selectedColor(Color color) {
        this.selectedColor = color;
    }
}
