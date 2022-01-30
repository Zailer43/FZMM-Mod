package fzmm.zailer.me.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;

import java.awt.*;

public class GradientLogic {
    public static MutableText getGradient(String message, byte red, byte green, byte blue, byte red2, byte green2, byte blue2, Style style) {
        int gradientLength = message.length();
        byte[] gradientRed = getByteGradient(red, red2, gradientLength);
        byte[] gradientGreen = getByteGradient(green, green2, gradientLength);
        byte[] gradientBlue = getByteGradient(blue, blue2, gradientLength);
        MutableText gradientText = LiteralText.EMPTY.copy();

        for (int i = 0; i != gradientLength; i++) {
            int rgb = rgbToInt(gradientRed[i], gradientGreen[i], gradientBlue[i]);
            gradientText.append(new LiteralText(String.valueOf(message.charAt(i)))
                    .setStyle(style
                            .withColor(TextColor.fromRgb(rgb))
                    ));
        }
        return gradientText;
    }

    public static byte[] getByteGradient(byte initialColor, byte finalColor, int gradientLength) {
        int gradientArrayLength = gradientLength - 1;
        byte[] gradient = new byte[gradientLength];
        for (int i = 0; i != gradientLength; i++) {
            gradient[i] = Byte.MIN_VALUE;
        }

        for (int i = 0; i != gradientLength; i++) {
            float percentage = (float) (gradientArrayLength - i) / gradientArrayLength, //[86, 77, 68...
                    reversePercentage = (float) i / gradientArrayLength; //[0, 23, 46...

            gradient[i] += (byte) (initialColor * percentage);
            gradient[i] += (byte) (finalColor * reversePercentage);
        }

        return gradient;
    }

    private static int rgbToInt(byte red, byte green, byte blue) {
        return new Color(Byte.toUnsignedInt(red), Byte.toUnsignedInt(green), Byte.toUnsignedInt(blue), 0).getRGB();
    }

    public static MutableText hexRgbToMutableText(String message, String initialColor, String finalColor, boolean obfuscated, boolean bold, boolean strikethrough, boolean underline, boolean italic) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        byte red = (byte) (Integer.valueOf(initialColor.substring(0, 2), 16) + Byte.MIN_VALUE);
        byte green = (byte) (Integer.valueOf(initialColor.substring(2, 4), 16) + Byte.MIN_VALUE);
        byte blue = (byte) (Integer.valueOf(initialColor.substring(4, 6), 16) + Byte.MIN_VALUE);
        byte red2 = (byte) (Integer.valueOf(finalColor.substring(0, 2), 16) + Byte.MIN_VALUE);
        byte green2 = (byte) (Integer.valueOf(finalColor.substring(2, 4), 16) + Byte.MIN_VALUE);
        byte blue2 = (byte) (Integer.valueOf(finalColor.substring(4, 6), 16) + Byte.MIN_VALUE);

        Style style = Style.EMPTY;
        if (obfuscated)
            style = style.withObfuscated(true);
        if (bold)
            style = style.withBold(true);
        if (strikethrough)
            style = style.withStrikethrough(true);
        if (underline)
            style = style.withUnderline(true);
        if (italic)
            style = style.withItalic(true);
        else
            style = style.withItalic(false);

        return getGradient(message, red, green, blue, red2, green2, blue2, style);
    }

}
