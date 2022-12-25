package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.toast.status.ImageStatus;
import fzmm.zailer.me.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class ImageUrlSource implements IImageSource {
    private BufferedImage image;

    public ImageUrlSource() {
        this.image = null;
    }

    @Override
    public ImageStatus loadImage(String value) {
        try {
            if (value.isEmpty())
                return ImageStatus.NO_IMAGE_LOADED;
            Optional<BufferedImage> optionalImage = ImageUtils.getImageFromUrl(value);
            optionalImage.ifPresent(image -> this.image = image);
            return optionalImage.isEmpty() ? ImageStatus.URL_HAS_NO_IMAGE : ImageStatus.IMAGE_LOADED;
        } catch (Exception e) {
            e.printStackTrace();
            return ImageStatus.MALFORMED_URL;
        }
    }

    @Override
    public BufferedImage getImage() {
        return this.image;
    }

    @Override
    public boolean predicate(String value) {
        return true;
    }
}
