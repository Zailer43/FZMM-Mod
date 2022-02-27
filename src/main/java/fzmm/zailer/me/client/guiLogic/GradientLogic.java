package fzmm.zailer.me.client.guiLogic;

import fi.dy.masa.malilib.util.Color4f;
import fzmm.zailer.me.client.gui.GradientScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class GradientLogic {
    public static MutableText getGradient(String message, byte red, byte green, byte blue, byte red2, byte green2, byte blue2, Style style) {
        List<String> messageList = splitString(message);
        int gradientLength = messageList.size();
        byte[] gradientRed = getByteGradient(red, red2, gradientLength);
        byte[] gradientGreen = getByteGradient(green, green2, gradientLength);
        byte[] gradientBlue = getByteGradient(blue, blue2, gradientLength);
        MutableText gradientText = LiteralText.EMPTY.copy();

        for (int i = 0; i != gradientLength; i++) {
            int rgb = rgbToInt(gradientRed[i], gradientGreen[i], gradientBlue[i]);
            gradientText.append(new LiteralText(String.valueOf(messageList.get(i)))
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

    public static MutableText getGradient(String message, Color4f initialColor4f, Color4f finalColor4f, boolean obfuscated, boolean bold, boolean strikethrough, boolean underline, boolean italic) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        Color initialColor = new Color(initialColor4f.intValue);
        Color finalColor = new Color(finalColor4f.intValue);

        byte red = (byte) (initialColor.getRed() + Byte.MIN_VALUE);
        byte green = (byte) (initialColor.getGreen() + Byte.MIN_VALUE);
        byte blue = (byte) (initialColor.getBlue() + Byte.MIN_VALUE);
        byte red2 = (byte) (finalColor.getRed() + Byte.MIN_VALUE);
        byte green2 = (byte) (finalColor.getGreen() + Byte.MIN_VALUE);
        byte blue2 = (byte) (finalColor.getBlue() + Byte.MIN_VALUE);

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

    //split string well with multibyte characters
    public static List<String> splitString(String data) {
        return Arrays.asList(data.split("(?s)(?<=\\G.{1," + GradientScreen.MAX_MESSAGE_LENGTH + "})"));
    }

}
