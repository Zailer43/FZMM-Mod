package fzmm.zailer.me.client.gui.imagetext.algorithms;

import fzmm.zailer.me.client.gui.components.tabs.ITab;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.utils.ImageUtils;

import java.awt.image.BufferedImage;

public interface IImagetextAlgorithm extends IMemento, ITab {

    @Override
    String getId();

    @Override
    default String getTranslationKey() {
        return "fzmm.gui.imagetext.tab.algorithm." + this.getId();
    }

    BufferedImage image();

    void image(BufferedImage image);

    void build();

    String[] linePixels(int line);

    /**
     * Sometimes it is necessary to know which characters are used in the lines,
     * because implementations can be character-width dependent.<br>
     * <br>
     * Example use cases include:<br>
     * - BookPage needs to know how many characters can fit on a page.
     * @return If all characters are of same width, any character is ok,
     * otherwise if is a sequence of characters, all are needed
     */
    String pixelExample();

    default int colorAt(int x, int y) {
        return this.image().getRGB(x, y);
    }

    /**
     * @return Factor of the width aspect ratio with respect to the height
     */
    float widthRatio();

    /**
     * @return Factor of the height aspect ratio with respect to the width
     */
    float heightRatio();

    void setUpdatePreviewCallback(Runnable callback);

    /**
     * Updates the cached image if necessary
     * @return true if cache was cleared
     */
    default boolean tryUpdateCache(ImagetextData data) {
        BufferedImage image = this.image();
        if (image == null || image.getWidth() != data.width() || image.getHeight() != data.height()) {
            this.clearCache();
            this.image(ImageUtils.fastResizeImage(data.image(), data.width(), data.height(), data.smoothRescaling()));
            return true;
        }
        return false;
    }

    default void clearCache() {
        BufferedImage image = this.image();
        if (image != null) {
            image.flush();
            this.image(null);
        }
    }
}
