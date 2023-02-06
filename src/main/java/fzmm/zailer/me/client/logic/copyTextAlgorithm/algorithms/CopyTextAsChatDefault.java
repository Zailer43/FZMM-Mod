package fzmm.zailer.me.client.logic.copyTextAlgorithm.algorithms;

import fzmm.zailer.me.client.logic.copyTextAlgorithm.AbstractCopyTextGeneric;
import net.minecraft.text.Style;

public class CopyTextAsChatDefault extends AbstractCopyTextGeneric {
    @Override
    public String getId() {
        return "chatDefault";
    }

    @Override
    public String getColor(Style style) {
        return style.getColor() == null ? "" : "&" + style.getColor().getHexCode();
    }
}
