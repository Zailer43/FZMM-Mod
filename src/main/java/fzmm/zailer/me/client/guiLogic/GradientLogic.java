package fzmm.zailer.me.client.guiLogic;

import fi.dy.masa.malilib.util.Color4f;
import fzmm.zailer.me.client.gui.GradientScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public record GradientLogic(String message, boolean obfuscated, boolean bold, boolean strikethrough, boolean underline, boolean italic) {

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

    public MutableText getGradient(Color4f initialColor4f, Color4f finalColor4f) {
        Color initialColor = new Color(initialColor4f.intValue);
        Color finalColor = new Color(finalColor4f.intValue);

        byte red = (byte) (initialColor.getRed() + Byte.MIN_VALUE);
        byte green = (byte) (initialColor.getGreen() + Byte.MIN_VALUE);
        byte blue = (byte) (initialColor.getBlue() + Byte.MIN_VALUE);
        byte red2 = (byte) (finalColor.getRed() + Byte.MIN_VALUE);
        byte green2 = (byte) (finalColor.getGreen() + Byte.MIN_VALUE);
        byte blue2 = (byte) (finalColor.getBlue() + Byte.MIN_VALUE);

        return getGradient(red, green, blue, red2, green2, blue2);
    }

    public MutableText getGradient(byte red, byte green, byte blue, byte red2, byte green2, byte blue2) {
        int messageLength = this.message.length();
        byte[] gradientRed = getByteGradient(red, red2, messageLength);
        byte[] gradientGreen = getByteGradient(green, green2, messageLength);
        byte[] gradientBlue = getByteGradient(blue, blue2, messageLength);
        int[] colors = new int[messageLength];

        for (int i = 0; i != messageLength; i++) {
            colors[i] = rgbToInt(gradientRed[i], gradientGreen[i], gradientBlue[i]);
        }
        return this.applyColors(colors);
    }

    private byte[] getByteGradient(byte initialColor, byte finalColor, int gradientLength) {
        int gradientArrayLength = gradientLength - 1;
        byte[] gradient = new byte[gradientLength];
        for (int i = 0; i != gradientLength; i++) {
            gradient[i] = Byte.MIN_VALUE;
        }

        for (int i = 0; i != gradientLength; i++) {
            float percentage = (float) (gradientArrayLength - i) / gradientArrayLength;//[86, 77, 68...
            float reversePercentage = (float) i / gradientArrayLength; //[0, 23, 46...

            gradient[i] += (byte) (initialColor * percentage);
            gradient[i] += (byte) (finalColor * reversePercentage);
        }

        return gradient;
    }

    private int rgbToInt(byte red, byte green, byte blue) {
        return new Color(Byte.toUnsignedInt(red), Byte.toUnsignedInt(green), Byte.toUnsignedInt(blue), 0).getRGB();
    }

    private MutableText applyColors(int[] colors) {
        MutableText text = LiteralText.EMPTY.copy().setStyle(this.getStyle());
        List<String> messageList = splitMessage();

        for (int i = 0; i != this.message.length(); i++)
            text.append(new LiteralText(messageList.get(i)).setStyle(Style.EMPTY.withColor(colors[i])));

        return text;
    }

    public Text getHsb(float hue, float saturation, float brightness, float hueStep) {
        int messageLength = this.message.length();
        int[] colors = new int[messageLength];

        for (int i = 0; i != messageLength; i++) {
            float hue2 = (hue + hueStep * i) % 1;
            colors[i] = Color.HSBtoRGB(hue2, saturation, brightness) - 0xFF000000;
        }

        return this.applyColors(colors);
    }

    /**
     * Split string well with multibyte characters
     */
    public List<String> splitMessage() {
        return Arrays.asList(this.message.split("(?s)(?<=\\G.{1," + GradientScreen.MAX_MESSAGE_LENGTH + "})"));
    }

}
