package fzmm.zailer.me.compat.placeholderApi;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.compat.CompatMods;
import net.minecraft.text.Text;

import java.lang.reflect.Method;

public class PlaceholderApiCompat {

    public static Text parse(String inputText) {
        Text result = Text.empty();
        try {
            Class<?> textParserUtilsClass = Class.forName("eu.pb4.placeholders.api.TextParserUtils");
            Method formatTextMethod = textParserUtilsClass.getDeclaredMethod("formatText", String.class);
            result = (Text) formatTextMethod.invoke(null, inputText);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[PlaceholderApiCompat] Failed to parse text", e);
            CompatMods.PLACEHOLDER_API_PRESENT = false;
        }

        return result;
    }
}
