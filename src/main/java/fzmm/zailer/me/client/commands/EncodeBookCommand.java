package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fzmm.zailer.me.config.FzmmConfig;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.MessageType;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.Date;
import java.util.Random;

public class EncodeBookCommand {

	public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
		return ArgumentBuilders.literal("encodebook")
			.then(ArgumentBuilders.argument("seed", IntegerArgumentType.integer()).executes(ctx -> {
				int seed = ctx.getArgument("seed", int.class);
				FzmmConfig config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();
				MinecraftClient mc = MinecraftClient.getInstance();
				assert mc.player != null;

				EncodeBook(seed, config.encodebook.defaultBookMessage, mc.player.getName().getString());
				return 1;

			}).then(ArgumentBuilders.argument("message", StringArgumentType.string()).executes(ctx -> {
				int seed = ctx.getArgument("seed", int.class);
				String message = ctx.getArgument("message", String.class);
				MinecraftClient mc = MinecraftClient.getInstance();
				assert mc.player != null;

				EncodeBook(seed, message, mc.player.getName().getString());
				return 1;

			}).then(ArgumentBuilders.argument("author", StringArgumentType.greedyString()).executes(ctx -> {
				int seed = ctx.getArgument("seed", int.class);
				String message = ctx.getArgument("message", String.class);
				String author = ctx.getArgument("author", String.class);

				EncodeBook(seed, message, author);
				return 1;
			}))));
	}

	public static int[] EncodeKey(long seed, int messageLength) {
		int i = 0;
		int[] encodeKey = new int[messageLength];
		Random number = new Random(seed);

		encodeKey[i] = number.nextInt(messageLength);
		for (; i < messageLength; i++) {
			encodeKey[i] = number.nextInt(messageLength);
			for (int j = 0; j < i; j++) {
				if (encodeKey[i] == encodeKey[j]) i--;
			}
		}

		return encodeKey;
	}

	public static void EncodeBook(int seed, String message, String author) {
		/*
		{
			title:"&3Encode book (secret_mc_1)",
			author:"Zailer43",
			pages:[
				'{
					"translate":"secret_mc_1",
					"with":[
						"y","F","r","d","6","5","8","y","s","A",...,"e","s"
					]
				}',
				'{
					"hoverEvent":{
						"action":"show_text",
						"contents":{
							"text":"yFrd658ysA...es"
						}
					},
					"text":"&9Idea by: &0turkeybot69\\n
					&9Encode key: &0secret_mc_1\\n
					&9End-to-end encode: &0true\\n
					&9Encode message: &0Hover over here"
				}'
			]
		}

		 */

		MinecraftClient mc = MinecraftClient.getInstance();
		FzmmConfig.Encodebook config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig().encodebook;
		Character[] encodeMessage = new Character[config.messageLength];
		int[] encodeKey;
		int messageLength;
		Random random = new Random(new Date().getTime());
		StringBuilder messageBuilder,
			encodeMessageString = new StringBuilder(),
			decoderString;
		String[] myRandom = config.myRandom.split("");
		ItemStack book = Items.WRITTEN_BOOK.getDefaultStack();
		CompoundTag tag = new CompoundTag();
		ListTag listTag = new ListTag();
		MutableText page1, page2, decoderMessage;
		assert mc.player != null;

		message += config.separatorMessage;
		message = message.replaceAll(" ", "_");
		messageBuilder = new StringBuilder(message);
		messageLength = message.length();
		encodeKey = EncodeKey(seed + config.endToEndEncodeKey, config.messageLength);

		while (messageLength < config.messageLength) {
			messageBuilder.append(myRandom[random.nextInt(myRandom.length)]);
			messageLength++;
		}

		message = messageBuilder.toString();
		for (int i = 0; i < config.messageLength; i++)
			encodeMessage[encodeKey[i]] = message.charAt(i);
		for (int i = 0; i < config.messageLength; i++) {
			encodeMessageString.append(encodeMessage[i]);
		}

		tag.putString("title", String.format(config.bookTitle, config.translationKey + seed));
		tag.putString("author", author);

		page1 = new TranslatableText(config.translationKey + seed, encodeMessage)
			.setStyle(Style.EMPTY
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("You probably need the decoder to see this message")))
			);

		page2 = new LiteralText(Formatting.BLUE + "Idea by: " + Formatting.BLACK + "turkeybot69\n" +
			Formatting.BLUE + "Encode key: " + Formatting.BLACK + config.translationKey + seed + "\n" +
			Formatting.BLUE + "End-to-end encode: " + Formatting.BLACK + (config.endToEndEncodeKey != 0) + "\n" +
			Formatting.BLUE + "Encode message: " + Formatting.BLACK + "Hover over here")
			.setStyle(Style.EMPTY
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(encodeMessageString.toString())))
			);

		listTag.add(StringTag.of(Text.Serializer.toJson(page1)));
		listTag.add(StringTag.of(Text.Serializer.toJson(page2)));
		tag.put("pages", listTag);
		book.setTag(tag);

		mc.player.equipStack(EquipmentSlot.MAINHAND, book);
		decoderString = new StringBuilder();

		for (int i = 0; i < config.messageLength; i++) {
			decoderString.append("%").append(encodeKey[i] + 1).append("$s");
		}

		decoderMessage = new LiteralText(Formatting.GREEN + config.translationKey + seed)
			.setStyle(Style.EMPTY
				.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "\"" + config.translationKey + seed + "\": \"" + decoderString + "\""))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(decoderString.toString())))
			);

		mc.inGameHud.addChatMessage(MessageType.SYSTEM, decoderMessage, mc.player.getUuid());
	}
}