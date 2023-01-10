package fzmm.zailer.me.client.logic.imagetext;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public final class ImagetextLineComponent {
    private final int pixelColor;
    private int repetitions;

    public ImagetextLineComponent(int pixelColor) {
        this.pixelColor = pixelColor;
        this.repetitions = 1;
    }

    public boolean tryAdd(int color, double percentageOfSimilarity) {
        if (!this.isSimilar(color, percentageOfSimilarity))
            return false;

        this.repetitions++;
        return true;
    }

    public boolean isSimilar(int color, double percentageOfSimilarity) {
        if (this.pixelColor == color)
            return true;

        int a1 = (this.pixelColor >> 24) & 0xff;
        int r1 = (this.pixelColor >> 16) & 0xff;
        int g1 = (this.pixelColor >> 8) & 0xff;
        int b1 = this.pixelColor & 0xff;

        int a2 = (color >> 24) & 0xff;
        int r2 = (color >> 16) & 0xff;
        int g2 = (color >> 8) & 0xff;
        int b2 = color & 0xff;

        int colorsDifference = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2) + Math.abs(a1 - a2);
        int colorSum = a1 + r1 + g1 + b1;
        return (colorsDifference * 100.0 / colorSum) < percentageOfSimilarity;
    }

    public int getColor() {
        return this.pixelColor;
    }

    public int getRepetitions() {
        return this.repetitions;
    }

    public Text getText(List<String> charactersToUse, int lineIndex, boolean isDefaultText) {
        int color = this.getColor();
        int alpha = (color >> 24) & 0xFF;
        return isDefaultText && alpha == 0 ? this.getEmptyText() : this.getText(charactersToUse, lineIndex);
    }

    public Text getEmptyText() {
        String spaceString = " ".repeat(this.repetitions);
        return Text.literal(spaceString + Formatting.BOLD + spaceString + Formatting.RESET);
    }

    private Text getText(List<String> charactersToUse, int lineIndex) {
        StringBuilder textStrBuilder = new StringBuilder();
        int red = (this.pixelColor >> 16) & 0xff;
        int green = (this.pixelColor >> 8) & 0xff;
        int blue = this.pixelColor & 0xff;
        int colorRGB = (red << 16) + (green << 8) + blue;

        for (int x = 0; x != this.repetitions; x++)
            textStrBuilder.append(this.getCharacter(charactersToUse, lineIndex++));

        return Text.literal(textStrBuilder.toString()).setStyle(Style.EMPTY.withColor(colorRGB));
    }

    private String getCharacter(List<String> charactersToUse, int index) {
        return charactersToUse.get(index % charactersToUse.size());
    }
}