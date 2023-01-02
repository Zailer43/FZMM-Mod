package fzmm.zailer.me.client.logic;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.*;

public class EncryptbookLogic {
    protected static List<Short> encryptKey(long seed, int messageLength) {
        List<Short> encryptedKey = new LinkedList<>();
        Random number = new Random(seed);

        // this is necessary to have backward compatibility with previous functionality
        number.nextInt(messageLength);

        while (encryptedKey.size() < messageLength) {
            short nextInt = (short) number.nextInt(messageLength);
            if (!encryptedKey.contains(nextInt))
                encryptedKey.add(nextInt);
        }

        return encryptedKey;
    }

    public static void give(int seed, String message, String author, String paddingChars, int maxMessageLength, String bookTitle) {
        FzmmConfig.Encryptbook config = FzmmClient.CONFIG.encryptbook;
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        List<Short> encryptedKey = encryptKey(getKey(seed), maxMessageLength);
        List<String> encryptMessage = encryptMessage(message, config, paddingChars, maxMessageLength, encryptedKey);
        String encryptMessageString = getEncryptMessageString(encryptMessage);

        ItemStack book = getBook(seed, config, bookTitle, author, encryptMessage, encryptMessageString);

        FzmmUtils.giveItem(book);
    }


    private static List<String> encryptMessage(String message, FzmmConfig.Encryptbook config, String paddingChars, int maxMessageLength, List<Short> encryptedKey) {
        Random random = new Random(new Date().getTime());
        List<String> paddingCharacters = Arrays.asList(paddingChars.split(""));

        message += config.separatorMessage();
        message = message.replaceAll(" ", "_");
        List<String> splitMessage = new ArrayList<>(FzmmUtils.splitMessage(message));
        int messageLength = splitMessage.size();

        while (messageLength < maxMessageLength) {
            String randomCharacter = paddingCharacters.get(random.nextInt(paddingCharacters.size()));
            splitMessage.add(randomCharacter);
            messageLength++;
        }

        List<String> encryptMessage = new ArrayList<>();
        for (int i = 0; i != splitMessage.size(); i++)
            encryptMessage.add("");

        for (int i = 0; i < maxMessageLength; i++)
            encryptMessage.set(encryptedKey.get(i), splitMessage.get(i));

        return encryptMessage;
    }

    private static String getEncryptMessageString(List<String> encryptMessage) {
        StringBuilder encryptMessageString = new StringBuilder();
        for (String s : encryptMessage)
            encryptMessageString.append(s);

        return encryptMessageString.toString();
    }

    private static ItemStack getBook(int seed, FzmmConfig.Encryptbook config, String bookTitle, String author, List<String> encryptMessage, String encryptMessageString) {
        String translationKeyPrefix = config.translationKeyPrefix();
        if (bookTitle.contains("%s")) {
            bookTitle = String.format(bookTitle, translationKeyPrefix + seed);
        }

        BookBuilder bookBuilder = BookBuilder.builder()
                .title(bookTitle)
                .author(author);

        bookBuilder.addPage(
                        Text.translatable(translationKeyPrefix + seed, encryptMessage.toArray())
                                .setStyle(Style.EMPTY
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("You probably need the decryptor to see this message")))
                                ))
                .addPage(Text.literal(Formatting.BLUE + "Idea by: " + Formatting.BLACK + "turkeybot69\n" +
                                Formatting.BLUE + "Key: " + Formatting.BLACK + translationKeyPrefix + seed + "\n" +
                                Formatting.BLUE + "Asymmetric: " + Formatting.BLACK + (config.asymmetricEncryptKey() != 0) + "\n" +
                                Formatting.BLUE + "Encrypted message: " + Formatting.BLACK + "Hover over here")
                        .setStyle(Style.EMPTY
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(encryptMessageString)))
                        ));

        return bookBuilder.get();
    }

    public static void showDecryptorInChat(final int SEED, final int MAX_MESSAGE_LENGTH) {
        MinecraftClient mc = MinecraftClient.getInstance();
        String translationKeyPrefix = FzmmClient.CONFIG.encryptbook.translationKeyPrefix();
        StringBuilder decryptorString = new StringBuilder();
        List<Short> encryptedKey = encryptKey(getKey(SEED), MAX_MESSAGE_LENGTH);

        assert mc.player != null;

        for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
            decryptorString.append("%").append(encryptedKey.get(i) + 1).append("$s");
        }

        MutableText decryptorMessage = Text.literal(Formatting.GREEN + translationKeyPrefix + SEED)
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "\"" + translationKeyPrefix + SEED + "\": \"" + decryptorString + "\""))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(decryptorString.toString())))
                );

        mc.inGameHud.getChatHud().addMessage(decryptorMessage);
    }

    private static long getKey(long seed) {
        int asymmetricEncryptKey = FzmmClient.CONFIG.encryptbook.asymmetricEncryptKey();
        return seed * (asymmetricEncryptKey != 0 ? (long) asymmetricEncryptKey + 0x19429630 : 1);
    }
}
