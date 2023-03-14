package fzmm.zailer.me.client.gui.options;

import fzmm.zailer.me.client.gui.components.IMode;
import net.minecraft.entity.decoration.DisplayEntity;

//TODO: look for a better way to do this without hardcoding the billboard values
public enum DisplayEntityBillboardOption implements IMode {
    FIXED(DisplayEntity.BillboardMode.FIXED),
    @SuppressWarnings("unused")
    CENTER(DisplayEntity.BillboardMode.CENTER),
    @SuppressWarnings("unused")
    VERTICAL(DisplayEntity.BillboardMode.VERTICAL),
    @SuppressWarnings("unused")
    HORIZONTAL(DisplayEntity.BillboardMode.HORIZONTAL);

    private final DisplayEntity.BillboardMode type;

    DisplayEntityBillboardOption(DisplayEntity.BillboardMode type) {
        this.type = type;
    }

    @Override
    public String getTranslationKey() {
        return "fzmm.gui.option.billboard." + this.type.asString();
    }

    public DisplayEntity.BillboardMode getType() {
        return this.type;
    }
}