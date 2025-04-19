package fzmm.zailer.me.client.gui.components.image.source;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.image.ImageStatus;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.skin.CacheSkinGetter;
import fzmm.zailer.me.utils.skin.SkinGetterDecorator;
import fzmm.zailer.me.utils.skin.VanillaSkinGetter;

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
        if (this.image != null) {
            this.image.flush();
        }
        this.image = null;

        try {
            SkinGetterDecorator skinGetter = new VanillaSkinGetter();

            if (this.predicateOnlinePlayer(value)) {
                skinGetter = new CacheSkinGetter(skinGetter);
            } else if (!this.isValidName(value)) {
                return ImageStatus.INVALID_USERNAME;
            }

            Optional<BufferedImage> optionalImage = ImageUtils.getPlayerSkin(value, skinGetter);
            optionalImage.ifPresent(image -> this.image = image);
            if (optionalImage.isEmpty()) {
                return this.predicateOnlinePlayer(value) ? ImageStatus.PLAYER_HAS_NO_SKIN : ImageStatus.PLAYER_NOT_FOUND;
            }

            return ImageStatus.IMAGE_LOADED;
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
        return this.isValidName(value) || this.predicateOnlinePlayer(value);
    }

    private boolean isValidName(String value) {
        return value.matches(REGEX);
    }

    private boolean predicateOnlinePlayer(String value) {
        // supports users that are not allowed by the regex, as long as that player is online
        return FzmmUtils.getOnlinePlayer(value) != null;
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
