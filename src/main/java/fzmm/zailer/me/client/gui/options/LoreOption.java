package fzmm.zailer.me.client.gui.options;

import fzmm.zailer.me.client.gui.components.IMode;

public enum LoreOption implements IMode {
    ADD("add"),
    @SuppressWarnings("unused")
    SET("set");

    private final String name;

    LoreOption(String name) {
        this.name = name;
    }

    @Override
    public String getTranslationKey() {
        return "fzmm.gui.option.lore." + this.name;
    }
}