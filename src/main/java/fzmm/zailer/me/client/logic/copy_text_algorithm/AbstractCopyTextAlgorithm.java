package fzmm.zailer.me.client.logic.copy_text_algorithm;

import fzmm.zailer.me.utils.SnackBarManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.List;

public abstract class AbstractCopyTextAlgorithm {

    public abstract String getId();

    public void copy(Component text) {
        String value = this.getString(text);
        SnackBarManager.copyToClipboard(value);
    }

    public String getString(Component text) {
        Style baseStyle = text.getStyle();

        StringBuilder stringBuilder = new StringBuilder();
        List<Component> siblings = text.getSiblings();
        this.getStringRecursive(stringBuilder, baseStyle, !siblings.isEmpty() ? siblings : List.of(text));

        return stringBuilder.toString();
    }

    protected abstract void getStringRecursive(StringBuilder stringBuilder, Style baseStyle, List<Component> siblings);
}
