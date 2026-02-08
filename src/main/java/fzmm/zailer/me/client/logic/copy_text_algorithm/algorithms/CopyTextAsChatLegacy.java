package fzmm.zailer.me.client.logic.copy_text_algorithm.algorithms;

import fzmm.zailer.me.client.logic.copy_text_algorithm.AbstractCopyTextGeneric;
import net.minecraft.network.chat.Style;

public class CopyTextAsChatLegacy extends AbstractCopyTextGeneric {
    @Override
    public String getId() {
        return "chatLegacy";
    }

    @Override
    public String getColor(Style style) {
        if (style.getColor() == null)
            return "";
        String hexCode = style.getColor().formatValue().substring(1);
        return this.colorCharacter() + "x" + this.colorCharacter() + String.join(this.colorCharacter(), hexCode.split(""));
    }
}
