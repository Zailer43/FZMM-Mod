package fzmm.zailer.me.client.logic.copyTextAlgorithm.algorithms;

import fzmm.zailer.me.client.logic.copyTextAlgorithm.AbstractCopyTextAlgorithm;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

public class CopyTextAsBBCode extends AbstractCopyTextAlgorithm {
    @Override
    public String getId() {
        return "bbcode";
    }

    @Override
    protected void getStringRecursive(StringBuilder stringBuilder, Style baseStyle, List<Text> siblings) {
        for (var value : siblings) {
            Style style = value.getStyle();
            if (style.getColor() != null)
                stringBuilder.append("[COLOR=").append(style.getColor().getHexCode()).append("]");
            stringBuilder.append(value.getString());
            if (style.getColor() != null)
                stringBuilder.append("[/COLOR]");

            if (!value.getSiblings().isEmpty())
                this.getStringRecursive(stringBuilder, baseStyle, value.getSiblings());
        }
    }

}
