package fzmm.zailer.me.client.gui.options;

import fzmm.zailer.me.client.gui.components.IMode;
import net.minecraft.block.WoodType;

//TODO: look for a better way to do this without hardcoding the wood types
public enum SignTypeOption implements IMode {
    OAK(WoodType.OAK),
    @SuppressWarnings("unused")
    SPRUCE(WoodType.SPRUCE),
    @SuppressWarnings("unused")
    BIRCH(WoodType.BIRCH),
    @SuppressWarnings("unused")
    ACACIA(WoodType.ACACIA),
    @SuppressWarnings("unused")
    CHERRY(WoodType.CHERRY),
    @SuppressWarnings("unused")
    JUNGLE(WoodType.JUNGLE),
    @SuppressWarnings("unused")
    DARK_OAK(WoodType.DARK_OAK),
    @SuppressWarnings("unused")
    CRIMSON(WoodType.CRIMSON),
    @SuppressWarnings("unused")
    WARPED(WoodType.WARPED),
    @SuppressWarnings("unused")
    MANGROVE(WoodType.MANGROVE),
    @SuppressWarnings("unused")
    BAMBOO(WoodType.BAMBOO);

    private final WoodType type;

    SignTypeOption(WoodType type) {
        this.type = type;
    }

    @Override
    public String getTranslationKey() {
        return "block.minecraft." + this.type.name() + "_sign";
    }

    public WoodType getType() {
        return this.type;
    }
}