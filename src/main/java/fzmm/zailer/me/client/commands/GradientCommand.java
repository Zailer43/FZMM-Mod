package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.MessageType;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.regex.Pattern;

public class GradientCommand {
	public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
		return ArgumentBuilders.literal("gradient")
			.then(ArgumentBuilders.argument("message", StringArgumentType.string())

				.then(ArgumentBuilders.argument("red", IntegerArgumentType.integer(0, 255))
					.then(ArgumentBuilders.argument("green", IntegerArgumentType.integer(0, 255))
						.then(ArgumentBuilders.argument("blue", IntegerArgumentType.integer(0, 255))
							.then(ArgumentBuilders.argument("red2", IntegerArgumentType.integer(0, 255))
								.then(ArgumentBuilders.argument("green2", IntegerArgumentType.integer(0, 255))
									.then(ArgumentBuilders.argument("blue2", IntegerArgumentType.integer(0, 255))
										.executes(ctx -> {

											String message = ctx.getArgument("message", String.class);
											int red = ctx.getArgument("red", int.class);
											int green = ctx.getArgument("green", int.class);
											int blue = ctx.getArgument("blue", int.class);
											int red2 = ctx.getArgument("red2", int.class);
											int green2 = ctx.getArgument("green2", int.class);
											int blue2 = ctx.getArgument("blue2", int.class);

											displayGradient(getGradient(message, red, green, blue, red2, green2, blue2));

											return 1;
										})))))))
				.then(ArgumentBuilders.argument("RRGGBB", StringArgumentType.word())
					.then(ArgumentBuilders.argument("RRGGBB 2", StringArgumentType.word())
						.executes(ctx -> {

							String message = ctx.getArgument("message", String.class);
							String RRGGBB = ctx.getArgument("RRGGBB", String.class);
							String RRGGBB2 = ctx.getArgument("RRGGBB 2", String.class);

							hexRGBSubCommand(message, RRGGBB, RRGGBB2);
							return 1;
						}))));
	}

	public static int[] getArrayIntGradient(int initialColor, int finalColor, int gradientLength) {
		int gradientArrayLength = gradientLength - 1;
		int[] gradient = new int[gradientLength];
		for (int i = 0; i != gradientLength; i++) {
			gradient[i] = 0;
			gradient[i] = 0;
		}

		for (int i = 0; i != gradientLength; i++) {
			float percentage = (float) (gradientArrayLength - i) / gradientArrayLength, //[86, 77, 68...
				reversePercentage = (float) i / gradientArrayLength; //[0, 23, 46...

			gradient[i] += (int) (initialColor * percentage);
			gradient[i] += (int) (finalColor * reversePercentage);
		}

		return gradient;
	}

	public static MutableText[] getGradient(String message, int red, int green, int blue, int red2, int green2, int blue2) {
		int gradientLength = message.length();
		int[] gradientRed = getArrayIntGradient(red, red2, gradientLength),
			gradientBlue = getArrayIntGradient(blue, blue2, gradientLength),
			gradientGreen = getArrayIntGradient(green, green2, gradientLength);
		MutableText[] gradientMessage = new MutableText[gradientLength];

		for (int i = 0; i != gradientLength; i++) {
			int rgb = (gradientRed[i] * 65536) + (gradientGreen[i] * 256) + gradientBlue[i];
			gradientMessage[i] = new LiteralText(message.charAt(i) + "")
				.setStyle(Style.EMPTY
					.withColor(TextColor.fromRgb(rgb))
				);
		}
		return gradientMessage;
	}

	public static void hexRGBSubCommand(String message, String RRGGBB, String RRGGBB2) {
		String regex = "^[0-9,a-f]{6}$";
		MinecraftClient mc = MinecraftClient.getInstance();
		assert mc.player != null;

		if (RRGGBB.length() != 6 || RRGGBB2.length() != 6) {
			Text error = new LiteralText(Formatting.RED + "Los colores deben tener una longitud de 6 caracteres hexadecimales");

			mc.inGameHud.addChatMessage(MessageType.SYSTEM, error, mc.player.getUuid());
			return;
		} else if (!RRGGBB.matches(regex) || !RRGGBB2.matches(regex)) {
			Text error = new LiteralText(Formatting.RED + "Los colores deben ser hexadecimales");

			mc.inGameHud.addChatMessage(MessageType.SYSTEM, error, mc.player.getUuid());
			return;
		}

		int red = Integer.valueOf(RRGGBB.substring(0, 2), 16);
		int green = Integer.valueOf(RRGGBB.substring(2, 4), 16);
		int blue = Integer.valueOf(RRGGBB.substring(4, 6), 16);
		int red2 = Integer.valueOf(RRGGBB2.substring(0, 2), 16);
		int green2 = Integer.valueOf(RRGGBB2.substring(2, 4), 16);
		int blue2 = Integer.valueOf(RRGGBB2.substring(4, 6), 16);

		displayGradient(getGradient(message, red, green, blue, red2, green2, blue2));

	}

	public static void displayGradient(MutableText[] gradient) {
		MinecraftClient mc = MinecraftClient.getInstance();
		assert mc.player != null;
		ListTag listTag = new ListTag();
		String gradientTag;
		MutableText message = new LiteralText("");

		for (int i = 0; i != gradient.length; i++) {
			listTag.add(StringTag.of(Text.Serializer.toJson(gradient[i])));
			message.append(gradient[i]);
		}
		gradientTag = listTag.asString().replaceAll("'", "");

		message.setStyle(Style.EMPTY
			.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, gradientTag))
			.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(Formatting.BLUE + "Clic aquí para copiar")))
		);

		mc.inGameHud.addChatMessage(MessageType.SYSTEM, message, mc.player.getUuid());
	}
}
