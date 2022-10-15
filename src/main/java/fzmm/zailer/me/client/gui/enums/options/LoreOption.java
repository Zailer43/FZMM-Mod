package fzmm.zailer.me.client.gui.enums.options;

import fzmm.zailer.me.client.gui.widgets.IMode;
import net.minecraft.text.Text;

public enum LoreOption implements IMode {
    ADD("add"),
    SET("set");

    private final String name;

    LoreOption(String name) {
        this.name = name;
    }

    @Override
    public Text getTranslation() {
        return Text.translatable("fzmm.gui.option.lore." + this.name);
    }
}