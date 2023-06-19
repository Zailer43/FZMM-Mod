package fzmm.zailer.me.compat.placeholderApi;

import eu.pb4.placeholders.api.TextParserUtils;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.compat.CompatMods;
import net.minecraft.text.Text;

public class PlaceholderApiCompat {

    public static Text parse(String inputText) {
        if (!CompatMods.PLACEHOLDER_API_PRESENT)
            return Text.literal(inputText);


        try {
            return TextParserUtils.formatText(inputText);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[PlaceholderApiCompat] Failed to parse text", e);
            CompatMods.PLACEHOLDER_API_PRESENT = false;
            return Text.literal(inputText);
        }
    }
}
