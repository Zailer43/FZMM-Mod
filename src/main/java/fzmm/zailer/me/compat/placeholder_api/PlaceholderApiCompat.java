package fzmm.zailer.me.compat.placeholder_api;

import eu.pb4.placeholders.api.parsers.TagParser;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.compat.CompatMods;
import net.minecraft.network.chat.Component;

public class PlaceholderApiCompat {

    public static Component parse(String inputText) {
        if (!CompatMods.PLACEHOLDER_API_PRESENT)
            return Component.literal(inputText);


        try {
            return TagParser.DEFAULT.parseNode(inputText).toComponent();
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[PlaceholderApiCompat] Failed to parse text", e);
            CompatMods.PLACEHOLDER_API_PRESENT = false;
            return Component.literal(inputText);
        }
    }
}
