package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.toast.status.ImageStatus;

import java.awt.image.BufferedImage;
import java.util.Optional;

public interface IImageSource {

    ImageStatus loadImage(String value);

    Optional<BufferedImage> getImage();

    boolean predicate(String value);
}
