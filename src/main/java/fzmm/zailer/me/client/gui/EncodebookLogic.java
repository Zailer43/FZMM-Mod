package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.config.FzmmConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.MessageType;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.Date;
import java.util.Random;

public class EncodebookLogic {
	protected static short[] EncodeKey(long seed, int messageLength) {
		short i = 0;
		short[] encodeKey = new short[messageLength];
		Random number = new Random(seed);

		encodeKey[i] = (short) number.nextInt(messageLength);
		for (; i < messageLength; i++) {
			encodeKey[i] = (short) number.nextInt(messageLength);
			for (int j = 0; j < i; j++) {
				if (encodeKey[i] == encodeKey[j]) i--;
			}
		}

		return encodeKey;
	}

	protected static void EncodeBook(final int SEED, String message, final String AUTHOR, final String PADDING_CHARS, final short MAX_MESSAGE_LENGTH, final String BOOK_TITLE) {
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
		Character[] encodeMessage = new Character[MAX_MESSAGE_LENGTH];
		short[] encodeKey;
		Random random = new Random(new Date().getTime());
		StringBuilder messageBuilder,
			encodeMessageString = new StringBuilder();
		String[] myRandom = PADDING_CHARS.split("");
		ItemStack book = Items.WRITTEN_BOOK.getDefaultStack();
		NbtCompound tag = new NbtCompound();
		NbtList NbtList = new NbtList();
		MutableText page1, page2;
		assert mc.player != null;

		message += config.separatorMessage;
		message = message.replaceAll(" ", "_");
		messageBuilder = new StringBuilder(message);
		int messageLength = message.length();
		encodeKey = EncodeKey(SEED + config.endToEndEncodeKey, MAX_MESSAGE_LENGTH);

		while (messageLength < MAX_MESSAGE_LENGTH) {
			messageBuilder.append(myRandom[random.nextInt(myRandom.length)]);
			messageLength++;
		}

		message = messageBuilder.toString();
		for (int i = 0; i < MAX_MESSAGE_LENGTH; i++)
			encodeMessage[encodeKey[i]] = message.charAt(i);
		for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
			encodeMessageString.append(encodeMessage[i]);
		}

		tag.putString("title", String.format(BOOK_TITLE, config.translationKey + SEED));
		tag.putString("author", AUTHOR);

		page1 = new TranslatableText(config.translationKey + SEED, encodeMessage)
			.setStyle(Style.EMPTY
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("You probably need the decoder to see this message")))
			);

		page2 = new LiteralText(Formatting.BLUE + "Idea by: " + Formatting.BLACK + "turkeybot69\n" +
			Formatting.BLUE + "Encode key: " + Formatting.BLACK + config.translationKey + SEED + "\n" +
			Formatting.BLUE + "End-to-end encode: " + Formatting.BLACK + (config.endToEndEncodeKey != 0) + "\n" +
			Formatting.BLUE + "Encode message: " + Formatting.BLACK + "Hover over here")
			.setStyle(Style.EMPTY
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(encodeMessageString.toString())))
			);

		NbtList.add(NbtString.of(Text.Serializer.toJson(page1)));
		NbtList.add(NbtString.of(Text.Serializer.toJson(page2)));
		tag.put("pages", NbtList);
		book.setTag(tag);

		mc.player.equipStack(EquipmentSlot.MAINHAND, book);
	}

	protected static void showDecoderInChat(final int SEED, final short MAX_MESSAGE_LENGTH) {
		MinecraftClient mc = MinecraftClient.getInstance();
		FzmmConfig.Encodebook config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig().encodebook;
		StringBuilder decoderString = new StringBuilder();
		short[] encodeKey = EncodeKey(SEED + config.endToEndEncodeKey, MAX_MESSAGE_LENGTH);

		assert mc.player != null;

		for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
			decoderString.append("%").append(encodeKey[i] + 1).append("$s");
		}

		MutableText decoderMessage = new LiteralText(Formatting.GREEN + config.translationKey + SEED)
			.setStyle(Style.EMPTY
				.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "\"" + config.translationKey + SEED + "\": \"" + decoderString + "\""))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(decoderString.toString())))
			);

		mc.inGameHud.addChatMessage(MessageType.SYSTEM, decoderMessage, mc.player.getUuid());
	}
}