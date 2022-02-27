package fzmm.zailer.me.client.gui.enums.options;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;

public enum SkinOption implements IConfigOptionListEntry {
    NAME("name"),
    PATH("path");

    private final String name;

    SkinOption(String name) {
        this.name = name;
    }

    @Override
    public String getStringValue() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return new TranslatableText("fzmm.gui.option.skin." + this.name).getString();
    }

    @Override
    public IConfigOptionListEntry cycle(boolean forward) {
        return this == NAME ? PATH : NAME;
    }

    @Override
    public IConfigOptionListEntry fromString(String value) {
        for (SkinOption option : SkinOption.values()) {
            if (option.getStringValue().equalsIgnoreCase(value)) {
                return option;
            }
        }

        return NAME;
    }

    public int getWidth() {
        return MinecraftClient.getInstance().textRenderer.getWidth(this.getDisplayName());
    }
}