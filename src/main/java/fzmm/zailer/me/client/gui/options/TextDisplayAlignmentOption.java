package fzmm.zailer.me.client.gui.options;

import fzmm.zailer.me.client.gui.components.IMode;
import net.minecraft.entity.decoration.DisplayEntity;

//TODO: look for a better way to do this without hardcoding the alignment values
public enum TextDisplayAlignmentOption implements IMode {
    CENTER(DisplayEntity.TextDisplayEntity.TextAlignment.CENTER),
    @SuppressWarnings("unused")
    LEFT(DisplayEntity.TextDisplayEntity.TextAlignment.LEFT),
    @SuppressWarnings("unused")
    RIGHT(DisplayEntity.TextDisplayEntity.TextAlignment.RIGHT);

    private final DisplayEntity.TextDisplayEntity.TextAlignment type;

    TextDisplayAlignmentOption(DisplayEntity.TextDisplayEntity.TextAlignment type) {
        this.type = type;
    }

    @Override
    public String getTranslationKey() {
        return "fzmm.gui.option.text_alignment." + this.type.asString();
    }

    public DisplayEntity.TextDisplayEntity.TextAlignment getType() {
        return this.type;
    }
}