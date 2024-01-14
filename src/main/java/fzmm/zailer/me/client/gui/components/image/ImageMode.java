package fzmm.zailer.me.client.gui.components.image;

import fzmm.zailer.me.client.gui.components.image.source.*;

import java.util.function.Supplier;

public enum ImageMode {
    URL("url", ImageUrlSource::new, false),
    @SuppressWarnings("unused")
    SCREENSHOT("screenshot", ScreenshotSource::new, false),
    @SuppressWarnings("unused")
    PATH("path", ImageFileSource::new, false),

    NAME("name",ImagePlayerNameSource::new, true),
    @SuppressWarnings("unused")
    HEAD("head", PlayerHeadSource::new, false);

    private static final String BASE_TRANSLATION_KEY = "fzmm.gui.option.image.";
    private final String id;
    private final Supplier<IImageGetter> sourceTypeSupplier;
    private final boolean isHeadName;

    ImageMode(String id, Supplier<IImageGetter> sourceTypeSupplier, boolean isHeadName) {
        this.id = id;
        this.sourceTypeSupplier = sourceTypeSupplier;
        this.isHeadName = isHeadName;
    }

    public String getTranslationKey() {
        return BASE_TRANSLATION_KEY + this.id;
    }

    public IImageGetter getImageGetter() {
        return this.sourceTypeSupplier.get();
    }

    public boolean isHeadName() {
        return this.isHeadName;
    }

}
