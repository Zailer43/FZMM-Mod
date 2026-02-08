package fzmm.zailer.me.client.logic.imagetext;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.awt.*;

public final class ImagetextLineElement {
    private int elementColor;
    private short a; // i want unsigned bytes :(
    private short r;
    private short g;
    private short b;
    private int colorSum;
    private short repetitions;
    private boolean isEmptyText;

    public ImagetextLineElement(int pixelColor, boolean isDefaultText) {
        this.reset(pixelColor, isDefaultText);
    }

    public void reset(int color, boolean isDefaultText) {
        this.elementColor = color;
        this.a = (short) (color >> 24 & 0xFF);
        this.r = (short) (color >> 16 & 0xFF);
        this.g = (short) (color >> 8 & 0xFF);
        this.b = (short) (color & 0xFF);
        this.colorSum = this.a + this.r + this.g + this.b;
        this.repetitions = 1;
        this.isEmptyText = isDefaultText && this.a < 128;
    }

    public void increment() {
        this.repetitions++;
    }

    public boolean isSimilar(int color, double percentageOfSimilarity) {
        if (this.elementColor == color) return true;

        Color colorObj = new Color(color, true);
        int a2 = colorObj.getAlpha();
        int r2 = colorObj.getRed();
        int g2 = colorObj.getGreen();
        int b2 = colorObj.getBlue();

        int colorsDifference = Math.abs(this.r - r2) + Math.abs(this.g - g2) + Math.abs(this.b - b2) + Math.abs(this.a - a2);
        return (colorsDifference * 100.0 / this.colorSum) < percentageOfSimilarity;
    }

    public short getRepetitions() {
        return this.repetitions;
    }

    public Component toText(String[] charactersToUse, int lineIndex) {
        if (this.isEmptyText) return this.toEmptyText();

        StringBuilder textStrBuilder = new StringBuilder();
        int colorRGB = this.elementColor & 0x00FFFFFF;

        for (int x = 0; x != this.repetitions; x++) {
            textStrBuilder.append(this.getCharacter(charactersToUse, lineIndex++));
        }

        return Component.literal(textStrBuilder.toString()).setStyle(Style.EMPTY.withColor(colorRGB));
    }

    private Component toEmptyText() {
        String spaceString = " ".repeat(this.repetitions);
        return Component.literal(spaceString + ChatFormatting.BOLD + spaceString + ChatFormatting.RESET);
    }

    private String getCharacter(String[] charactersToUse, int index) {
        return charactersToUse[index % charactersToUse.length];
    }
}