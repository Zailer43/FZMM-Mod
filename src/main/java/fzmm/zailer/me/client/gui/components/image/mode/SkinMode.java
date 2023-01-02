package fzmm.zailer.me.client.gui.components.image.mode;

import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;
import fzmm.zailer.me.client.gui.components.image.source.ImageFileSource;
import fzmm.zailer.me.client.gui.components.image.source.ImagePlayerNameSource;
import net.minecraft.text.Text;

public enum SkinMode implements IImageMode {
    NAME("name", new ImagePlayerNameSource()),
    @SuppressWarnings("unused")
    PATH("path", new ImageFileSource());

    private static final String BASE_TRANSLATION_KEY = "fzmm.gui.option.skin.";
    private final String translationKey;
    private final IImageGetter sourceType;

    SkinMode(String translationKey, IImageGetter sourceType) {
        this.translationKey = translationKey;
        this.sourceType = sourceType;
    }

    public Text getTranslation() {
        return Text.translatable(BASE_TRANSLATION_KEY + this.translationKey);
    }

    @Override
    public IImageGetter getImageGetter() {
        return this.sourceType;
    }
}
