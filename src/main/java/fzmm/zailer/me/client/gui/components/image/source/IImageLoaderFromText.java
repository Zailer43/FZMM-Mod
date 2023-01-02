package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.toast.status.ImageStatus;

public interface IImageLoaderFromText extends IImageGetter {

    ImageStatus loadImage(String value);

    boolean predicate(String value);
}
