package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.toast.status.ImageStatus;
import fzmm.zailer.me.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;

public class ImageFileSource implements IImageSource {
    private BufferedImage image;

    public ImageFileSource() {
        this.image = null;
    }

    @Override
    public ImageStatus loadImage(String value) {
        try {
            File file = Paths.get(value).toFile();
            if (!file.exists())
                return ImageStatus.FILE_DOES_NOT_EXIST;

            if (!file.isFile())
                return ImageStatus.PATH_DOES_NOT_HAVE_A_FILE;

            this.image = ImageUtils.getImageFromPath(value);
            return ImageStatus.IMAGE_LOADED;
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
        File file = new File(value);
        return file.exists() && file.isFile();
    }
}
