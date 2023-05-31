package fzmm.zailer.me.client.logic.headGenerator.model.steps.fillColor;

import io.wispforest.owo.ui.core.Color;

public class FillColorMultiply implements IFillColorAlgorithm {
    @Override
    public int getColor(Color selectedColor, int pixelColor) {
        int alpha = (pixelColor >> 24) & 0xFF;
        if (alpha == 0)
            return pixelColor;

        int origRed = (pixelColor >> 16) & 0xFF;
        int origGreen = (pixelColor >> 8) & 0xFF;
        int origBlue = pixelColor & 0xFF;

        int newRed = (int) (origRed * selectedColor.red());
        int newGreen = (int) (origGreen * selectedColor.green());
        int newBlue = (int) (origBlue * selectedColor.blue());

        return new Color(newRed / 255f, newGreen / 255f, newBlue / 255f, alpha / 255f).argb();
    }
}
