package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.toast.status.ImageStatus;
import fzmm.zailer.me.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class ImagePlayerNameSource implements IImageSource {
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
            e.printStackTrace();
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
}
