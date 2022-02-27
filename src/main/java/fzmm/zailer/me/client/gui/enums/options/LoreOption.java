package fzmm.zailer.me.client.gui.enums.options;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import net.minecraft.text.TranslatableText;

public enum LoreOption implements IConfigOptionListEntry {
    ADD("add"),
    SET("set");

    private final String name;

    LoreOption(String name) {
        this.name = name;
    }

    @Override
    public String getStringValue() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return new TranslatableText("fzmm.gui.option.lore." + this.name).getString();
    }

    @Override
    public IConfigOptionListEntry cycle(boolean forward) {
        return this == ADD ? SET : ADD;
    }

    @Override
    public IConfigOptionListEntry fromString(String value) {
        for (fzmm.zailer.me.client.gui.enums.options.BookOption option : fzmm.zailer.me.client.gui.enums.options.BookOption.values()) {
            if (option.getStringValue().equalsIgnoreCase(value)) {
                return option;
            }
        }

        return ADD;
    }
}