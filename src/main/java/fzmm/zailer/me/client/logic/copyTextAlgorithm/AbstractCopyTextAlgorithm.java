package fzmm.zailer.me.client.logic.copyTextAlgorithm;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

public abstract class AbstractCopyTextAlgorithm {

    public abstract String getId();

    public void copy(Text text) {
        String value = this.getString(text);
        MinecraftClient.getInstance().keyboard.setClipboard(value);
    }

    public String getString(Text text) {
        Style baseStyle = text.getStyle();

        StringBuilder stringBuilder = new StringBuilder();
        List<Text> siblings = text.getSiblings();
        this.getStringRecursive(stringBuilder, baseStyle, siblings.size() > 0 ? siblings : List.of(text));

        return stringBuilder.toString();
    }

    protected abstract void getStringRecursive(StringBuilder stringBuilder, Style baseStyle, List<Text> siblings);
}
