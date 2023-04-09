package fzmm.zailer.me.client.logic.headGenerator.model;

import fzmm.zailer.me.client.logic.headGenerator.model.parameters.ModelParameter;
import io.wispforest.owo.ui.core.Color;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public record ModelData(Graphics2D graphics, List<ModelParameter<BufferedImage>> textures, List<ModelParameter<Color>> colors,
                        AtomicReference<BufferedImage> selectedTexture, AtomicReference<Color> selectedColor) {

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
}
