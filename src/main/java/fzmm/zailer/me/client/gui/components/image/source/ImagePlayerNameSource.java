package fzmm.zailer.me.client.gui.components.image.source;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.toast.status.ImageStatus;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class ImagePlayerNameSource implements IImageLoaderFromText, IImageSuggestion {
    private static final String REGEX = "^[a-zA-Z0-9_]{2,16}$";
    private BufferedImage image;

    public ImagePlayerNameSource() {
        this.image = null;
    }

    @Override
    public ImageStatus loadImage(String value) {
        this.image = null;

        try {
            if (!this.predicate(value))
                return ImageStatus.INVALID_USERNAME;

            Optional<BufferedImage> optionalImage = ImageUtils.getPlayerSkin(value);
            optionalImage.ifPresent(image -> this.image = image);
            return optionalImage.isEmpty() ? ImageStatus.INVALID_USERNAME : ImageStatus.IMAGE_LOADED;

        } catch (Exception e) {
            FzmmClient.LOGGER.error("Unexpected error loading an image", e);
            return ImageStatus.UNEXPECTED_ERROR;
        }
    }

    @Override
    public Optional<BufferedImage> getImage() {
        return Optional.ofNullable(this.image);
    }

    @Override
    public boolean predicate(String value) {
        return value.matches(REGEX);
    }

    @Override
    public boolean hasTextField() {
        return true;
    }

    @Override
    public SuggestionProvider<?> getSuggestionProvider() {
        return FzmmUtils.SUGGESTION_PLAYER;
    }
}
