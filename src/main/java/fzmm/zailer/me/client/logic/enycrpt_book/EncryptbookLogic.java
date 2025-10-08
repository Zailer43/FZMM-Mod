package fzmm.zailer.me.client.logic.enycrpt_book;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.TextUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.*;

public class EncryptbookLogic {
    public static void give(String message, String author, String paddingChars, String bookTitle, TranslationEncryptProfile profile, boolean addPage) {
        FzmmConfig.Encryptbook config = FzmmClient.CONFIG.encryptbook;

        List<Short> encryptedIndex = profile.encryptIndexOrder();
        List<String> encryptMessageSplit = encryptMessage(message, config, paddingChars, profile.length(), encryptedIndex);

        BookBuilder bookBuilder = null;
        if (addPage) {
            Optional<BookBuilder> builder = BookBuilder.of(ItemUtils.from(Hand.MAIN_HAND));
            if (builder.isPresent()) {
                bookBuilder = builder.get();
            }
        }
        if (bookBuilder == null) {
            bookBuilder = newBook(bookTitle, author, profile);
        }

        ItemStack book = bookAddPage(bookBuilder, encryptMessageSplit, profile).get();

        ItemUtils.give(book);
    }


    private static List<String> encryptMessage(String message, FzmmConfig.Encryptbook config, String paddingChars,
                                               int maxMessageLength, List<Short> encrypteIndex) {
        Random random = new Random(new Date().getTime());
        List<String> paddingCharacters = TextUtils.splitMessage(paddingChars);

        message += config.separatorMessage();
        message = message.replaceAll(" ", "_");
        List<String> splitMessage = new ArrayList<>(TextUtils.splitMessage(message));
        int paddingCharactersCount = paddingCharacters.size();

        for (int i = splitMessage.size(); i < maxMessageLength; i++) {
            String randomCharacter = paddingCharacters.get(random.nextInt(paddingCharactersCount));
            splitMessage.add(randomCharacter);
        }

        List<String> encryptMessage = new ArrayList<>(Collections.nCopies(maxMessageLength, null));

        for (int i = 0; i < maxMessageLength; i++) {
            encryptMessage.set(encrypteIndex.get(i), splitMessage.get(i));
        }

        return encryptMessage;
    }

    private static BookBuilder newBook(String bookTitle, String author, TranslationEncryptProfile profile) {
        bookTitle = bookTitle.replaceFirst("%s", profile.translationKey());

        return BookBuilder.builder()
                .title(bookTitle)
                .author(author);
    }

    private static BookBuilder bookAddPage(BookBuilder builder, List<String> encryptMessageSplit, TranslationEncryptProfile profile) {
        String translationKey = profile.translationKey();
        String encryptMessage = String.join("", encryptMessageSplit);

        Text encryptMessageTooltip = Text.literal(
                Text.translatable("fzmm.item.encryptbook.encryptMessage.tooltip", translationKey, profile.isAsymmetric()).getString()
        );

        builder.addPage(
                Text.translatableWithFallback(translationKey, encryptMessage, encryptMessageSplit.toArray())
                        .setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, encryptMessageTooltip)))
        );

        return builder;
    }
}
