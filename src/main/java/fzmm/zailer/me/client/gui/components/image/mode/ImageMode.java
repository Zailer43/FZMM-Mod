package fzmm.zailer.me.client.gui.components.image.mode;

import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;
import fzmm.zailer.me.client.gui.components.image.source.ImageFileSource;
import fzmm.zailer.me.client.gui.components.image.source.ImageUrlSource;
import fzmm.zailer.me.client.gui.components.image.source.ScreenshotSource;

import java.util.function.Supplier;

public enum ImageMode implements IImageMode {
    URL("url", ImageUrlSource::new),
    @SuppressWarnings("unused")
    SCREENSHOT("screenshot", ScreenshotSource::new),
    @SuppressWarnings("unused")
    PATH("path", ImageFileSource::new);

    private static final String BASE_TRANSLATION_KEY = "fzmm.gui.option.imageMode.";
    private final String translationKey;
    private final Supplier<IImageGetter> sourceTypeSupplier;

    ImageMode(String translationKey, Supplier<IImageGetter> sourceTypeSupplier) {
        this.translationKey = translationKey;
        this.sourceTypeSupplier = sourceTypeSupplier;
    }

    public String getTranslationKey() {
        return BASE_TRANSLATION_KEY + this.translationKey;
    }

    @Override
    public IImageGetter getImageGetter() {
        return this.sourceTypeSupplier.get();
    }
}
