package fzmm.zailer.me.client.gui.components.image.mode;

import fzmm.zailer.me.client.gui.components.IMode;
import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;

public interface IImageMode extends IMode {

    IImageGetter getImageGetter();
}
