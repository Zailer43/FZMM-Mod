package fzmm.zailer.me.client.gui.components.image.source;

import java.awt.image.BufferedImage;
import java.util.Optional;

public interface IImageGetter {

    Optional<BufferedImage> getImage();

    boolean hasTextField();
}
