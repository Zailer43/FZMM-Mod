package fzmm.zailer.me.client.logic.copy_text_algorithm.algorithms;

import fzmm.zailer.me.client.logic.copy_text_algorithm.AbstractCopyTextAlgorithm;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.List;

public class CopyTextAsBBCode extends AbstractCopyTextAlgorithm {
    @Override
    public String getId() {
        return "bbcode";
    }

    @Override
    protected void getStringRecursive(StringBuilder stringBuilder, Style baseStyle, List<Component> siblings) {
        for (var value : siblings) {
            Style style = value.getStyle();
            if (style.getColor() != null)
                stringBuilder.append("[COLOR=").append(style.getColor().formatValue()).append("]");
            stringBuilder.append(value.getString());
            if (style.getColor() != null)
                stringBuilder.append("[/COLOR]");

            if (!value.getSiblings().isEmpty())
                this.getStringRecursive(stringBuilder, baseStyle, value.getSiblings());
        }
    }

}
