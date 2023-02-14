package fzmm.zailer.me.compat.placeholderApi;

import net.minecraft.text.Text;

public class PlaceholderApiCompat {

    public static Text parse(String inputText) {
        return eu.pb4.placeholders.api.TextParserUtils.formatText(inputText);
    }
}
