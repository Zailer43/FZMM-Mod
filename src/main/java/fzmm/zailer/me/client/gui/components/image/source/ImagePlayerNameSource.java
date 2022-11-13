package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.toast.status.ImageStatus;
import fzmm.zailer.me.utils.FzmmUtils;

import java.awt.image.BufferedImage;

public class ImagePlayerNameSource implements IImageSource {
    private static final String REGEX = "^[a-zA-Z0-9_]{2,16}$";
    private BufferedImage image;

    public ImagePlayerNameSource() {
        this.image = null;
    }

    @Override
    public ImageStatus loadImage(String value) {
        try {
            if (!this.predicate(value))
                return ImageStatus.INVALID_USERNAME;

            this.image = FzmmUtils.getPlayerSkin(value);
            return this.image == null ? ImageStatus.INVALID_USERNAME : ImageStatus.IMAGE_LOADED;

        } catch (Exception e) {
            e.printStackTrace();
            return ImageStatus.UNEXPECTED_ERROR;
        }
    }

    @Override
    public BufferedImage getImage() {
        return this.image;
    }

    @Override
    public boolean predicate(String value) {
        return value.matches(REGEX);
    }
}