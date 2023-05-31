package fzmm.zailer.me.client.logic.headGenerator.model.steps.fillColor;

import io.wispforest.owo.ui.core.Color;

public class FillColorSolid implements IFillColorAlgorithm {
    @Override
    public int getColor(Color selectedColor, int pixelColor) {
        return selectedColor.rgb();
    }
}
