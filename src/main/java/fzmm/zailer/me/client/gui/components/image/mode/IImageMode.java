package fzmm.zailer.me.client.gui.components.image.mode;

import fzmm.zailer.me.client.gui.components.IMode;
import fzmm.zailer.me.client.gui.components.image.source.IImageSource;

public interface IImageMode extends IMode {

    IImageSource getSourceType();
}
