package fzmm.zailer.me.client.gui.widgets.image.source;

import fzmm.zailer.me.client.gui.widgets.image.ImageStatus;

import java.awt.image.BufferedImage;

public interface IImageSource {

    ImageStatus loadImage(String value);

    BufferedImage getImage();

    boolean predicate(String value);
}
