package fzmm.zailer.me.client.gui.options;

import fzmm.zailer.me.client.gui.components.IMode;
import net.minecraft.text.Text;
import net.minecraft.util.SignType;

//TODO: look for a better way to do this without hardcoding the sign types
public enum SignTypeOption implements IMode {
    OAK(SignType.OAK),
    @SuppressWarnings("unused")
    SPRUCE(SignType.SPRUCE),
    @SuppressWarnings("unused")
    BIRCH(SignType.BIRCH),
    @SuppressWarnings("unused")
    ACACIA(SignType.ACACIA),
    @SuppressWarnings("unused")
    JUNGLE(SignType.JUNGLE),
    @SuppressWarnings("unused")
    DARK_OAK(SignType.DARK_OAK),
    @SuppressWarnings("unused")
    CRIMSON(SignType.CRIMSON),
    @SuppressWarnings("unused")
    WARPED(SignType.WARPED),
    @SuppressWarnings("unused")
    MANGROVE(SignType.MANGROVE),
    @SuppressWarnings("unused")
    BAMBOO(SignType.BAMBOO);

    private final SignType type;

    SignTypeOption(SignType type) {
        this.type = type;
    }

    @Override
    public Text getTranslation() {
        return Text.translatable("block.minecraft." + this.type.getName() + "_sign");
    }

    public SignType getType() {
        return this.type;
    }
}