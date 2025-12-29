package fzmm.zailer.me.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class TextUtils {
    /**
     * Splits the characters of a message correctly including multibyte characters correctly
     */
    public static List<String> splitMessage(String message) {
        List<String> characters = new ArrayList<>(message.length());
        for (int i = 0; i < message.length(); ) {
            int codePoint = message.codePointAt(i);
            characters.add(new String(Character.toChars(codePoint)));
            i += Character.charCount(codePoint);
        }
        return characters;
    }

    /**
     * Removes unpaired multibyte characters<br>
     * Characters outside the BMP (Basic Multilingual Plane) are encoded as
     * surrogate pairs, lone surrogates are invalid and can break encoders/IO<br>
     */
    public static String removeUnpairedMultibyte(String message) {
        if (message.isBlank()) return message;

        StringBuilder builder = new StringBuilder(message.length());
        int codePoint;

        for (int i = 0; i < message.length(); i += Character.charCount(codePoint)) {
            codePoint = message.codePointAt(i);
            if (Character.charCount(codePoint) == 2 || !Character.isSurrogate(message.charAt(i))) {
                builder.appendCodePoint(codePoint);
            }
        }
        return builder.toString();
    }

    public static Optional<String> decodeBase64(String encoded) {
        try {
            byte[] decodedValue = Base64.getDecoder().decode(encoded);
            return Optional.of(new String(decodedValue, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public static String encodeBase64(String message) {
        byte[] messageByte = message.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(messageByte);
    }

    public static Component mergeText(List<Component> text) {
        MutableComponent result = Component.empty();

        int size = text.size();
        for (int i = 0; i != size; i++) {
            result.append(text.get(i));
            if (i != size - 1) {
                result.append("\n");
            }
        }

        return result;
    }
}
