package fzmm.zailer.me.client.logic.copyTextAlgorithm;

import net.minecraft.text.Style;

public class CopyTextAsChatLegacy extends AbstractCopyTextGeneric {
    @Override
    public String getId() {
        return "chatLegacy";
    }

    @Override
    public String getColor(Style style) {
        if (style.getColor() == null)
            return "";
        String hexCode = style.getColor().getHexCode().substring(1);
        return this.colorCharacter() + "x" + this.colorCharacter() + String.join(this.colorCharacter(), hexCode.split(""));
    }
}
