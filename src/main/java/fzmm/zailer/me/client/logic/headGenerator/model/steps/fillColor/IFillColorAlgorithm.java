package fzmm.zailer.me.client.logic.headGenerator.model.steps.fillColor;

import io.wispforest.owo.ui.core.Color;

public interface IFillColorAlgorithm {
    IFillColorAlgorithm SOLID = new FillColorSolid();
    IFillColorAlgorithm MULTIPLY = new FillColorMultiply();

    /**
     * @return ARGB color
     */
    int getColor(Color selectedColor, int pixelColor);
}
