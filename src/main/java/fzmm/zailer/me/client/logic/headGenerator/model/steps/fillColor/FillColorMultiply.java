package fzmm.zailer.me.client.logic.headGenerator.model.steps.fillColor;

import java.awt.*;

public class FillColorMultiply implements IFillColorAlgorithm {
    @Override
    public Color getColor(io.wispforest.owo.ui.core.Color selectedColor, int pixelColor) {
        int alpha = (pixelColor >> 24) & 0xFF;
        int origRed = (pixelColor >> 16) & 0xFF;
        int origGreen = (pixelColor >> 8) & 0xFF;
        int origBlue = pixelColor & 0xFF;

        int newRed = (int) (origRed * selectedColor.red());
        int newGreen = (int) (origGreen * selectedColor.green());
        int newBlue = (int) (origBlue * selectedColor.blue());

        return new Color(newRed, newGreen, newBlue, alpha);
    }
}
