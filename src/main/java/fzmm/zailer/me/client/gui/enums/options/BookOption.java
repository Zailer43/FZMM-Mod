package fzmm.zailer.me.client.gui.enums.options;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import net.minecraft.text.TranslatableText;

public enum BookOption implements IConfigOptionListEntry {
    ADD_PAGE("addPage"),
    CREATE_BOOK("createBook");

    private final String name;

    BookOption(String name) {
        this.name = name;
    }

    @Override
    public String getStringValue() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return new TranslatableText("fzmm.gui.option.book." + this.name).getString();
    }

    @Override
    public IConfigOptionListEntry cycle(boolean forward) {
        return this == ADD_PAGE ? CREATE_BOOK : ADD_PAGE;
    }

    @Override
    public IConfigOptionListEntry fromString(String value) {
        for (BookOption option : BookOption.values()) {
            if (option.getStringValue().equalsIgnoreCase(value)) {
                return option;
            }
        }

        return ADD_PAGE;
    }
}