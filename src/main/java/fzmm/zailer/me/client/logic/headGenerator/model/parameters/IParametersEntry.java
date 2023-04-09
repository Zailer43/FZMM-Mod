package fzmm.zailer.me.client.logic.headGenerator.model.parameters;

import io.wispforest.owo.ui.core.Color;

import java.awt.image.BufferedImage;
import java.util.List;

public interface IParametersEntry {

    List<ModelParameter<Color>> getColors();

    void putColor(String key, Color color);

    List<ModelParameter<BufferedImage>> getTextures();

    void putTexture(String key, BufferedImage image);

    boolean hasParameters();
}
