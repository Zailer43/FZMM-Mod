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

public class EncryptbookLogic {
	protected static short[] encryptKey(long seed, int messageLength) {
		short i = 0;
		short[] encryptedKey = new short[messageLength];
		Random number = new Random(seed);

		encryptedKey[i] = (short) number.nextInt(messageLength);
		for (; i < messageLength; i++) {
			encryptedKey[i] = (short) number.nextInt(messageLength);
			for (int j = 0; j < i; j++) {
				if (encryptedKey[i] == encryptedKey[j]) i--;
			}
		}

		return encryptedKey;
	}

	public static void give(final int SEED, String message, final String AUTHOR, final String PADDING_CHARS, final int MAX_MESSAGE_LENGTH, String bookTitle) {
		/*
		{
			title:"&Encryptbook (secret_mc_1)",
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
					&9Key: &0secret_mc_1\\n
					&9Asymmetric: &0true\\n
					&9Encrypted message: &0Hover over here"
				}'
			]
		}

		 */

		MinecraftClient mc = MinecraftClient.getInstance();
		Character[] encryptMessage = new Character[MAX_MESSAGE_LENGTH];
		short[] encryptedKey;
		Random random = new Random(new Date().getTime());
		StringBuilder messageBuilder,
			encryptMessageString = new StringBuilder();
		String[] paddingCharacters = PADDING_CHARS.split("");
		ItemStack book = Items.WRITTEN_BOOK.getDefaultStack();
		NbtCompound tag = new NbtCompound();
		NbtList pages = new NbtList();
		MutableText page1, page2;
		String translationKeyPrefix = Configs.Encryptbook.TRANSLATION_KEY_PREFIX.getStringValue();
		assert mc.player != null;

		message += Configs.Encryptbook.SEPARATOR_MESSAGE.getStringValue();
		message = message.replaceAll(" ", "_");
		messageBuilder = new StringBuilder(message);
		int messageLength = message.length();
		encryptedKey = encryptKey(getKey(SEED), MAX_MESSAGE_LENGTH);

		if (bookTitle.contains("%s")) {
			bookTitle = String.format(bookTitle, translationKeyPrefix + SEED);
		}

		while (messageLength < MAX_MESSAGE_LENGTH) {
			messageBuilder.append(paddingCharacters[random.nextInt(paddingCharacters.length)]);
			messageLength++;
		}

		message = messageBuilder.toString();
		for (int i = 0; i < MAX_MESSAGE_LENGTH; i++)
			encryptMessage[encryptedKey[i]] = message.charAt(i);
		for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
			encryptMessageString.append(encryptMessage[i]);
		}

		tag.putString(WrittenBookItem.TITLE_KEY, bookTitle);
		tag.putString(WrittenBookItem.AUTHOR_KEY, AUTHOR);

		page1 = new TranslatableText(translationKeyPrefix + SEED, (Object[]) encryptMessage)
			.setStyle(Style.EMPTY
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("You probably need the decryptor to see this message")))
			);

		page2 = new LiteralText(Formatting.BLUE + "Idea by: " + Formatting.BLACK + "turkeybot69\n" +
			Formatting.BLUE + "Key: " + Formatting.BLACK + translationKeyPrefix + SEED + "\n" +
			Formatting.BLUE + "Asymmetric: " + Formatting.BLACK + (Configs.Encryptbook.ASYMMETRIC_ENCRYPT_KEY.getIntegerValue() != 0) + "\n" +
			Formatting.BLUE + "Encrypted message: " + Formatting.BLACK + "Hover over here")
			.setStyle(Style.EMPTY
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(encryptMessageString.toString())))
			);

		pages.add(FzmmUtils.toNbtString(page1, false));
		pages.add(FzmmUtils.toNbtString(page2, false));
		tag.put(WrittenBookItem.PAGES_KEY, pages);
		book.setNbt(tag);

		FzmmUtils.giveItem(book);
	}

	public static void showDecryptorInChat(final int SEED, final int MAX_MESSAGE_LENGTH) {
		MinecraftClient mc = MinecraftClient.getInstance();
		String translationKeyPrefix = Configs.Encryptbook.TRANSLATION_KEY_PREFIX.getStringValue();
		StringBuilder decryptorString = new StringBuilder();
		short[] encryptedKey = encryptKey(getKey(SEED), MAX_MESSAGE_LENGTH);

		assert mc.player != null;

		for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
			decryptorString.append("%").append(encryptedKey[i] + 1).append("$s");
		}

		MutableText decryptorMessage = new LiteralText(Formatting.GREEN + translationKeyPrefix + SEED)
			.setStyle(Style.EMPTY
				.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "\"" + translationKeyPrefix + SEED + "\": \"" + decryptorString + "\""))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(decryptorString.toString())))
			);

		mc.inGameHud.addChatMessage(MessageType.SYSTEM, decryptorMessage, mc.player.getUuid());
	}

	private static long getKey(long seed) {
		int asymmetricEncryptKey = Configs.Encryptbook.ASYMMETRIC_ENCRYPT_KEY.getIntegerValue();
		return seed * (asymmetricEncryptKey != 0 ? (long) asymmetricEncryptKey + 0x19429630 : 1);
	}
}
