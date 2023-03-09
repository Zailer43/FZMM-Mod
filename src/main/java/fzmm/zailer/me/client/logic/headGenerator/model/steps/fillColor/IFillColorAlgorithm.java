package fzmm.zailer.me.client.logic.headGenerator.model.steps.fillColor;

import java.awt.*;

public interface IFillColorAlgorithm {
    IFillColorAlgorithm SOLID = new FillColorSolid();
    IFillColorAlgorithm MULTIPLY = new FillColorMultiply();

    Color getColor(io.wispforest.owo.ui.core.Color selectedColor, int pixelColor);
}
