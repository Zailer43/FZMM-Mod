package fzmm.zailer.me.client.gui.widgets.image.source;

import fzmm.zailer.me.client.gui.widgets.image.ImageStatus;
import fzmm.zailer.me.utils.FzmmUtils;

import java.awt.image.BufferedImage;

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
            this.image = FzmmUtils.getImageFromUrl(value);
            return this.image == null ? ImageStatus.URL_HAS_NO_IMAGE : ImageStatus.IMAGE_LOADED;
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
