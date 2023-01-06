package fzmm.zailer.me.client.logic;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record TextFormatLogic(String message, boolean obfuscated, boolean bold, boolean strikethrough,
                              boolean underline, boolean italic) {

    private Style getStyle() {
        Style style = Style.EMPTY.withItalic(this.italic);
        if (this.obfuscated)
            style = style.withObfuscated(true);
        if (this.bold)
            style = style.withBold(true);
        if (this.strikethrough)
            style = style.withStrikethrough(true);
        if (this.underline)
            style = style.withUnderline(true);

        return style;
    }

    public MutableText getGradient(List<Integer> colors) {
        if (colors.isEmpty())
            return Text.empty();

        int messageLength = FzmmUtils.splitMessage(this.message).size();
        int[][] colorComponents = this.getColorComponents(colors);
        int[][] gradientComponents = this.getGradientComponents(colorComponents, messageLength);
        int[] gradientColors = this.getGradientColors(gradientComponents);
        return this.applyColors(FzmmUtils.splitMessage(this.message), gradientColors);
    }


    /**
     * Calculates a gradient of colors based on an array of starting and ending color components.
     * @param colorComponents An array of color component arrays, where each array represents a
     *                       starting or ending color in the gradient.
     * @param messageLength The number of colors in the final gradient.
     * @return An array of color component arrays, representing the intermediate colors in the gradient.
     */
    public int[][] getGradientComponents(int[][] colorComponents, int messageLength) {
        List<int[]> gradient = new ArrayList<>();
        int minStep = messageLength / (colorComponents.length - 1);
        int maxStep = minStep + 1;
        int numMaxSteps = messageLength % (colorComponents.length - 1);
        for (int i = 0; i < colorComponents.length - 1; i++) {
            int[] startColor = colorComponents[i];
            int[] endColor = colorComponents[i + 1];
            int currentStep = i < numMaxSteps ? maxStep : minStep;
            for (int j = 0; j < currentStep; j++) {
                int[] currentColor = new int[3];
                currentColor[0] = startColor[0] + (endColor[0] - startColor[0]) * j / currentStep;
                currentColor[1] = startColor[1] + (endColor[1] - startColor[1]) * j / currentStep;
                currentColor[2] = startColor[2] + (endColor[2] - startColor[2]) * j / currentStep;
                gradient.add(currentColor);
            }
        }
        return gradient.toArray(new int[gradient.size()][]);
    }


    private int[][] getColorComponents(List<Integer> colors) {
        return colors.stream().map(color -> new int[]{
                (color >> 16) & 0xff,
                (color >> 8) & 0xff,
                color & 0xff
        }).toArray(int[][]::new);
    }

    private int[] getGradientColors(int[][] gradientComponents) {
        return Stream.of(gradientComponents)
                .mapToInt(components -> (components[0] << 16) | (components[1] << 8) | components[2])
                .toArray();
    }

    private MutableText applyColors(List<String> characters, int[] colors) {
        MutableText text = Text.empty().setStyle(this.getStyle());

        for (int i = 0; i != characters.size(); i++) {
            int color = 0;
            if (colors.length > i)
                color = colors[i];
            text.append(Text.literal(characters.get(i)).setStyle(Style.EMPTY.withColor(color)));
        }

        return text;
    }

    public Text getRainbow(float hue, float saturation, float brightness, float hueStep) {
        List<String> characters = FzmmUtils.splitMessage(this.message);
        int messageLength = characters.size();
        int[] colors = new int[messageLength];

        for (int i = 0; i != messageLength; i++) {
            float hue2 = (hue + hueStep * i) % 1;
            colors[i] = Color.HSBtoRGB(hue2, saturation, brightness) - 0xFF000000;
        }

        return this.applyColors(characters, colors);
    }

    public Text getWithColor(int color) {
        return Text.literal(this.message).setStyle(this.getStyle().withColor(color));
    }

    public Text getInterleaved(List<Integer> colors, int distance) {
        List<String> characters = FzmmUtils.splitMessage(this.message);

        List<String> messageSplit = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();

        // This is used to optimize the NBT so that the final text uses the same Text object if it has the same color,
        // especially if the distance between each of the colors is greater than 1. To do this, the message is divided
        // into several parts and the colors are applied in an interleaved manner in each of them. In this way,
        // the number of necessary Text objects is minimized and the efficiency of the process is improved.
        for (int i = 0; i != characters.size(); i++) {
            stringBuilder.append(characters.get(i));
            if (i % distance == distance - 1) {
                messageSplit.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            }
        }
        if (!stringBuilder.isEmpty())
            messageSplit.add(stringBuilder.toString());

        int messageLength = messageSplit.size();
        int[] finalColors = new int[messageLength];

        for (int i = 0; i != messageLength; i++)
            finalColors[i] = colors.get(i % colors.size());

        return this.applyColors(messageSplit, finalColors);
    }
}
