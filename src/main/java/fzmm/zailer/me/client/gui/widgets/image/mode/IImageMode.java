package fzmm.zailer.me.client.gui.widgets.image.mode;

import fzmm.zailer.me.client.gui.widgets.IMode;
import fzmm.zailer.me.client.gui.widgets.image.source.IImageSource;

public interface IImageMode extends IMode {

    IImageSource getSourceType();
}
