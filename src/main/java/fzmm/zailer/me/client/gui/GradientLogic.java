package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.config.FzmmConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class GradientLogic {
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

	public static MutableText[] getGradient(String message, byte red, byte green, byte blue, byte red2, byte green2, byte blue2, Style style) {
		int gradientLength = message.length();
		byte[] gradientRed = getByteGradient(red, red2, gradientLength),
			gradientBlue = getByteGradient(blue, blue2, gradientLength),
			gradientGreen = getByteGradient(green, green2, gradientLength);
		MutableText[] gradientMessage = new MutableText[gradientLength];

		for (int i = 0; i != gradientLength; i++) {
			int rgb = (Byte.toUnsignedInt(gradientRed[i]) * 65536) +
				(Byte.toUnsignedInt(gradientGreen[i]) * 256) +
				Byte.toUnsignedInt(gradientBlue[i]);
			gradientMessage[i] = new LiteralText(message.charAt(i) + "")
				.setStyle(style
					.withColor(TextColor.fromRgb(rgb))
				);
		}
		return gradientMessage;
	}

	public static Text[] hexRgbToMutableText(String message, String initialColor, String finalColor, boolean obfuscated, boolean bold, boolean strikethrough, boolean underline, boolean italic) {
		MinecraftClient mc = MinecraftClient.getInstance();
		assert mc.player != null;

		byte red = (byte) (Integer.valueOf(initialColor.substring(0, 2), 16) + Byte.MIN_VALUE),
			green = (byte) (Integer.valueOf(initialColor.substring(2, 4), 16) + Byte.MIN_VALUE),
			blue = (byte) (Integer.valueOf(initialColor.substring(4, 6), 16) + Byte.MIN_VALUE),
			red2 = (byte) (Integer.valueOf(finalColor.substring(0, 2), 16) + Byte.MIN_VALUE),
			green2 = (byte) (Integer.valueOf(finalColor.substring(2, 4), 16) + Byte.MIN_VALUE),
			blue2 = (byte) (Integer.valueOf(finalColor.substring(4, 6), 16) + Byte.MIN_VALUE);

		Style style = Style.EMPTY;
		if (obfuscated) style = style.withFormatting(Formatting.OBFUSCATED);
		if (bold) style = style.withBold(true);
		if (strikethrough) style = style.withFormatting(Formatting.STRIKETHROUGH);
		if (underline) style = style.withUnderline(true);
		if (italic)
			style = style.withItalic(true);
		else if (FzmmConfig.get().general.disableItalic)
			style = style.withItalic(false);

		return getGradient(message, red, green, blue, red2, green2, blue2, style);

	}

	public static MutableText getGradientMessage(MutableText[] gradient) {
		MinecraftClient mc = MinecraftClient.getInstance();
		assert mc.player != null;
		MutableText message = new LiteralText("");

		for (int i = 0; i != gradient.length; i++) {
			message.append(gradient[i]);
		}

		return message;
	}
}
