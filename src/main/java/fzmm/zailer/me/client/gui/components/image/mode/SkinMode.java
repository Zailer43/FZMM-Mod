package fzmm.zailer.me.client.gui.components.image.mode;

import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;
import fzmm.zailer.me.client.gui.components.image.source.ImageFileSource;
import fzmm.zailer.me.client.gui.components.image.source.ImagePlayerNameSource;

public enum SkinMode implements IImageMode {
    NAME("name", new ImagePlayerNameSource(), true),
    @SuppressWarnings("unused")
    PATH("path", new ImageFileSource(), false);

    private static final String BASE_TRANSLATION_KEY = "fzmm.gui.option.skin.";
    private final String translationKey;
    private final IImageGetter sourceType;
    private final boolean isHeadName;

    SkinMode(String translationKey, IImageGetter sourceType, boolean isHeadName) {
        this.translationKey = translationKey;
        this.sourceType = sourceType;
        this.isHeadName = isHeadName;
    }

    public String getTranslationKey() {
        return BASE_TRANSLATION_KEY + this.translationKey;
    }

    @Override
    public IImageGetter getImageGetter() {
        return this.sourceType;
    }

    public boolean isHeadName() {
        return this.isHeadName;
    }
}
