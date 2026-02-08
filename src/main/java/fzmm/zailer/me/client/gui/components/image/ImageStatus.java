package fzmm.zailer.me.client.gui.components.image;

import fzmm.zailer.me.client.gui.components.extend.EStyles;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public record ImageStatus(String titleId, @Nullable String detailsId, boolean isError) {
    private static final String BASE_TRANSLATION_KEY = "fzmm.snack_bar.image.";
    public static final ImageStatus IMAGE_LOADED = new ImageStatus("successfully.title", null, false);
    public static final ImageStatus INVALID_USERNAME = new ImageStatus("error.title", "error.details.invalidUsername", true);
    public static final ImageStatus PLAYER_NOT_FOUND = new ImageStatus("error.title", "error.details.playerNotFound", true);
    public static final ImageStatus PLAYER_HAS_NO_SKIN = new ImageStatus("error.title", "error.details.playerHasNoSkin", true);
    public static final ImageStatus MALFORMED_URL = new ImageStatus("error.title", "error.details.malformedUrl", true);
    public static final ImageStatus NO_IMAGE_LOADED = new ImageStatus("error.title", "error.details.noImageLoaded", true);
    public static final ImageStatus UNEXPECTED_ERROR = new ImageStatus("error.title", "error.details.unexpectedError", true);
    public static final ImageStatus URL_HAS_NO_IMAGE = new ImageStatus("error.title", "error.details.urlHasNoImage", true);

    public Component getStatusTranslation() {
        return Component.translatable(BASE_TRANSLATION_KEY + this.titleId);
    }

    public boolean hasDetails() {
        return this.detailsId != null;
    }

    public Component getDetailsTranslation() {
        return Component.translatable(this.getDetailsTranslationKey());
    }

    private String getDetailsTranslationKey() {
        return BASE_TRANSLATION_KEY + this.detailsId;
    }

    public Color getColor() {
        return this.isError() ? EStyles.ALERT_ERROR_COLOR : EStyles.ALERT_SUCCESS_COLOR;
    }

    public boolean isError() {
        return this.isError;
    }
}