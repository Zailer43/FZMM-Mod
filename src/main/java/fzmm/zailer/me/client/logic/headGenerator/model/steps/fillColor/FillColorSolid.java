package fzmm.zailer.me.client.logic.headGenerator.model.steps.fillColor;

import java.awt.*;

public class FillColorSolid implements IFillColorAlgorithm {
    @Override
    public Color getColor(io.wispforest.owo.ui.core.Color selectedColor, int pixelColor) {
        return new Color(selectedColor.rgb());
    }
}
