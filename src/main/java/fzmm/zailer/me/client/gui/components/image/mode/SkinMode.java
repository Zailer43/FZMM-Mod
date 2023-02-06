package fzmm.zailer.me.client.gui.components.image.mode;

import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;
import fzmm.zailer.me.client.gui.components.image.source.ImageFileSource;
import fzmm.zailer.me.client.gui.components.image.source.ImagePlayerNameSource;

import java.util.function.Supplier;

public enum SkinMode implements IImageMode {
    NAME("name", ImagePlayerNameSource::new, true),
    @SuppressWarnings("unused")
    PATH("path", ImageFileSource::new, false);

    private static final String BASE_TRANSLATION_KEY = "fzmm.gui.option.skin.";
    private final String translationKey;
    private final Supplier<IImageGetter> sourceTypeSupplier;
    private final boolean isHeadName;

    SkinMode(String translationKey, Supplier<IImageGetter> sourceTypeSupplier, boolean isHeadName) {
        this.translationKey = translationKey;
        this.sourceTypeSupplier = sourceTypeSupplier;
        this.isHeadName = isHeadName;
    }

    public String getTranslationKey() {
        return BASE_TRANSLATION_KEY + this.translationKey;
    }

    @Override
    public IImageGetter getImageGetter() {
        return this.sourceTypeSupplier.get();
    }

    public boolean isHeadName() {
        return this.isHeadName;
    }
}
