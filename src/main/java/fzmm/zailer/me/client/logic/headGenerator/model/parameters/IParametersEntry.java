package fzmm.zailer.me.client.logic.headGenerator.model.parameters;

import io.wispforest.owo.ui.core.Color;

import java.awt.image.BufferedImage;
import java.util.List;

public interface IParametersEntry {

    List<? extends IModelParameter<Color>> getColors();

    void putColor(String key, Color color);

    List<ResettableModelParameter<BufferedImage, String>> getTextures();

    void putTexture(String key, BufferedImage image);

    List<? extends IModelParameter<OffsetParameter>> getOffsets();

    boolean hasParameters();
}
