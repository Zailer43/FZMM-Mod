package fzmm.zailer.me.client.guiLogic;

import fzmm.zailer.me.config.Configs;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.MessageType;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.Date;
import java.util.Random;

public class EncodebookLogic {
	protected static short[] encodeKey(long seed, int messageLength) {
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

	public static void EncodeBook(final int SEED, String message, final String AUTHOR, final String PADDING_CHARS, final int MAX_MESSAGE_LENGTH, String bookTitle) {
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
		Character[] encodeMessage = new Character[MAX_MESSAGE_LENGTH];
		short[] encodedKey;
		Random random = new Random(new Date().getTime());
		StringBuilder messageBuilder,
			encodeMessageString = new StringBuilder();
		String[] paddingCharacters = PADDING_CHARS.split("");
		ItemStack book = Items.WRITTEN_BOOK.getDefaultStack();
		NbtCompound tag = new NbtCompound();
		NbtList pages = new NbtList();
		MutableText page1, page2;
		String translationKeyPrefix = Configs.Encodebook.TRANSLATION_KEY_PREFIX.getStringValue();
		assert mc.player != null;

		message += Configs.Encodebook.SEPARATOR_MESSAGE.getStringValue();
		message = message.replaceAll(" ", "_");
		messageBuilder = new StringBuilder(message);
		int messageLength = message.length();
		encodedKey = encodeKey(getKey(SEED), MAX_MESSAGE_LENGTH);

		if (bookTitle.contains("%s")) {
			bookTitle = String.format(bookTitle, translationKeyPrefix + SEED);
		}

		while (messageLength < MAX_MESSAGE_LENGTH) {
			messageBuilder.append(paddingCharacters[random.nextInt(paddingCharacters.length)]);
			messageLength++;
		}

		message = messageBuilder.toString();
		for (int i = 0; i < MAX_MESSAGE_LENGTH; i++)
			encodeMessage[encodedKey[i]] = message.charAt(i);
		for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
			encodeMessageString.append(encodeMessage[i]);
		}

		tag.putString(WrittenBookItem.TITLE_KEY, bookTitle);
		tag.putString(WrittenBookItem.AUTHOR_KEY, AUTHOR);

		page1 = new TranslatableText(translationKeyPrefix + SEED, (Object[]) encodeMessage)
			.setStyle(Style.EMPTY
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("You probably need the decoder to see this message")))
			);

		page2 = new LiteralText(Formatting.BLUE + "Idea by: " + Formatting.BLACK + "turkeybot69\n" +
			Formatting.BLUE + "Encode key: " + Formatting.BLACK + translationKeyPrefix + SEED + "\n" +
			Formatting.BLUE + "Asymmetric encode: " + Formatting.BLACK + (Configs.Encodebook.ASYMMETRIC_ENCODE_KEY.getIntegerValue() != 0) + "\n" +
			Formatting.BLUE + "Encode message: " + Formatting.BLACK + "Hover over here")
			.setStyle(Style.EMPTY
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(encodeMessageString.toString())))
			);

		pages.add(FzmmUtils.textToNbtString(page1, false));
		pages.add(FzmmUtils.textToNbtString(page2, false));
		tag.put(WrittenBookItem.PAGES_KEY, pages);
		book.setNbt(tag);

		FzmmUtils.giveItem(book);
	}

	public static void showDecoderInChat(final int SEED, final int MAX_MESSAGE_LENGTH) {
		MinecraftClient mc = MinecraftClient.getInstance();
		String translationKeyPrefix = Configs.Encodebook.TRANSLATION_KEY_PREFIX.getStringValue();
		StringBuilder decoderString = new StringBuilder();
		short[] encodeKey = encodeKey(getKey(SEED), MAX_MESSAGE_LENGTH);

		assert mc.player != null;

		for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
			decoderString.append("%").append(encodeKey[i] + 1).append("$s");
		}

		MutableText decoderMessage = new LiteralText(Formatting.GREEN + translationKeyPrefix + SEED)
			.setStyle(Style.EMPTY
				.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "\"" + translationKeyPrefix + SEED + "\": \"" + decoderString + "\""))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(decoderString.toString())))
			);

		mc.inGameHud.addChatMessage(MessageType.SYSTEM, decoderMessage, mc.player.getUuid());
	}

	private static long getKey(long seed) {
		int asymmetricEncodeKey = Configs.Encodebook.ASYMMETRIC_ENCODE_KEY.getIntegerValue();
		return seed * (asymmetricEncodeKey != 0 ? (long) asymmetricEncodeKey + 0x19429630 : 1);
	}
}
