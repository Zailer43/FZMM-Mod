package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandException;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.MessageType;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.Random;

public class GradientCommand {
	static final CommandException ERROR_LENGTH = new CommandException(new TranslatableText("commands.fzmm.gradient.error.length"));
	static final CommandException ERROR_CHARACTER = new CommandException(new TranslatableText("commands.fzmm.gradient.error.character"));

	public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
		return ArgumentBuilders.literal("gradient")
			.then(ArgumentBuilders.argument("message", StringArgumentType.string())
				.executes(ctx -> {
					String message = ctx.getArgument("message", String.class);
					Random random = new Random();
					byte red = (byte) (random.nextInt(255) + Byte.MIN_VALUE),
						green = (byte) (random.nextInt(255) + Byte.MIN_VALUE),
						blue = (byte) (random.nextInt(255) + Byte.MIN_VALUE),
						red2 = (byte) (random.nextInt(255) + Byte.MIN_VALUE),
						green2 = (byte) (random.nextInt(255) + Byte.MIN_VALUE),
						blue2 = (byte) (random.nextInt(255) + Byte.MIN_VALUE);

					displayGradient(getGradient(message, red, green, blue, red2, green2, blue2));
					return 1;
				})
				.then(ArgumentBuilders.argument("red", IntegerArgumentType.integer(0, 255))
					.then(ArgumentBuilders.argument("green", IntegerArgumentType.integer(0, 255))
						.then(ArgumentBuilders.argument("blue", IntegerArgumentType.integer(0, 255))
							.then(ArgumentBuilders.argument("red2", IntegerArgumentType.integer(0, 255))
								.then(ArgumentBuilders.argument("green2", IntegerArgumentType.integer(0, 255))
									.then(ArgumentBuilders.argument("blue2", IntegerArgumentType.integer(0, 255))
										.executes(ctx -> {

											String message = ctx.getArgument("message", String.class);
											byte red = (byte) (ctx.getArgument("red", int.class) + Byte.MIN_VALUE),
												green = (byte) (ctx.getArgument("green", int.class) + Byte.MIN_VALUE),
												blue = (byte) (ctx.getArgument("blue", int.class) + Byte.MIN_VALUE),
												red2 = (byte) (ctx.getArgument("red2", int.class) + Byte.MIN_VALUE),
												green2 = (byte) (ctx.getArgument("green2", int.class) + Byte.MIN_VALUE),
												blue2 = (byte) (ctx.getArgument("blue2", int.class) + Byte.MIN_VALUE);

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

	public static byte[] getArrayIntGradient(byte initialColor, byte finalColor, int gradientLength) {
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

	public static MutableText[] getGradient(String message, byte red, byte green, byte blue, byte red2, byte green2, byte blue2) {
		int gradientLength = message.length();
		byte[] gradientRed = getArrayIntGradient(red, red2, gradientLength),
			gradientBlue = getArrayIntGradient(blue, blue2, gradientLength),
			gradientGreen = getArrayIntGradient(green, green2, gradientLength);
		MutableText[] gradientMessage = new MutableText[gradientLength];

		for (int i = 0; i != gradientLength; i++) {
			int rgb = (Byte.toUnsignedInt(gradientRed[i]) * 65536) +
				(Byte.toUnsignedInt(gradientGreen[i]) * 256) +
				Byte.toUnsignedInt(gradientBlue[i]);
			gradientMessage[i] = new LiteralText(message.charAt(i) + "")
				.setStyle(Style.EMPTY
					.withColor(TextColor.fromRgb(rgb))
				);
		}
		return gradientMessage;
	}

	public static void hexRGBSubCommand(String message, String RRGGBB, String RRGGBB2) {
		String regex = "^[0-9a-fA-F]{6}$";
		MinecraftClient mc = MinecraftClient.getInstance();
		assert mc.player != null;

		if (RRGGBB.length() != 6 || RRGGBB2.length() != 6) {
			throw ERROR_LENGTH;
		} else if (!RRGGBB.matches(regex) || !RRGGBB2.matches(regex)) {
			throw ERROR_CHARACTER;
		}

		byte red = (byte) (Integer.valueOf(RRGGBB.substring(0, 2), 16) + Byte.MIN_VALUE),
			green = (byte) (Integer.valueOf(RRGGBB.substring(2, 4), 16) + Byte.MIN_VALUE),
			blue = (byte) (Integer.valueOf(RRGGBB.substring(4, 6), 16) + Byte.MIN_VALUE),
			red2 = (byte) (Integer.valueOf(RRGGBB2.substring(0, 2), 16) + Byte.MIN_VALUE),
			green2 = (byte) (Integer.valueOf(RRGGBB2.substring(2, 4), 16) + Byte.MIN_VALUE),
			blue2 = (byte) (Integer.valueOf(RRGGBB2.substring(4, 6), 16) + Byte.MIN_VALUE);

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
