package fzmm.zailer.me.client.gui.components.image.source;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public interface IInteractiveImageLoader extends IImageGetter {

    void execute(Consumer<BufferedImage> consumer);

    void setImage(BufferedImage image);
}
