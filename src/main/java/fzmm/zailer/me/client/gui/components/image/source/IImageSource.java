package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.toast.status.ImageStatus;

import java.awt.image.BufferedImage;

public interface IImageSource {

    ImageStatus loadImage(String value);

    BufferedImage getImage();

    boolean predicate(String value);
}
