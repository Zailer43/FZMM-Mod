package fzmm.zailer.me.client.gui.components.image;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;
import fzmm.zailer.me.client.gui.components.image.source.IImageLoaderFromText;
import fzmm.zailer.me.client.gui.components.image.source.IInteractiveImageLoader;
import fzmm.zailer.me.client.gui.components.image.source.ImagePlayerNameSource;
import fzmm.zailer.me.client.toast.LoadingImageToast;
import fzmm.zailer.me.client.toast.status.ImageStatus;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class ImageButtonComponent extends ButtonComponent {

    private @Nullable BufferedImage image;
    private IImageGetter mode;
    private Function<BufferedImage, ImageStatus> imageLoadEvent;
    private Consumer<BufferedImage> callback;

    public ImageButtonComponent() {
        super(Text.empty(), button -> {
        });
        this.verticalSizing(Sizing.fixed(20));
        this.image = null;
        this.mode = new ImagePlayerNameSource();
        this.imageLoadEvent = null;
    }

    public Optional<BufferedImage> getImage() {
        return Optional.ofNullable(this.image);
    }

    public void setSourceType(IImageGetter mode) {
        this.mode = mode;
    }

    public boolean hasImage() {
        return this.image != null;
    }

    public void loadImage(String value) {
        if (this.mode instanceof IImageLoaderFromText imageLoaderFromText)
            this.loadImageFromText(imageLoaderFromText, value);
        else if (this.mode instanceof IInteractiveImageLoader interactiveImageLoader)
            this.interactiveImageLoad(interactiveImageLoader);
    }

    public void loadImageFromText(IImageLoaderFromText imageLoaderFromText, String value) {
        MinecraftClient.getInstance().execute(() -> {
            this.active = false;
            LoadingImageToast toast = new LoadingImageToast();
            MinecraftClient.getInstance().getToastManager().add(toast);
            ImageStatus status = imageLoaderFromText.loadImage(value);
            Optional<BufferedImage> image = imageLoaderFromText.getImage();

            if (status.statusType() == ImageStatus.StatusType.SUCCESSFUL) {
                if (this.imageLoadEvent != null) {
                    assert image.isPresent();
                    status = this.imageLoadEvent.apply(image.get());
                }
                FzmmClient.LOGGER.info("[ImageButtonComponent] image loaded successfully");
            } else {
                FzmmClient.LOGGER.error("[ImageButtonComponent] failed to load image");
            }

            this.active = true;
            toast.setResponse(status);

            this.image = image.orElse(null);
            if (this.callback != null)
                this.callback.accept(this.image);
        });
    }

    public void interactiveImageLoad(IInteractiveImageLoader interactiveImageLoader) {
        interactiveImageLoader.execute(bufferedImage -> {
            this.image = bufferedImage;

            this.active = true;

            if (this.callback != null)
                this.callback.accept(this.image);
        });
    }

    /**
     * @param callback The event will only fire if the image is loaded successfully,
     *                 the callback return will overwrite the status
     */
    public void setImageLoadedEvent(Function<BufferedImage, ImageStatus> callback) {
        this.imageLoadEvent = callback;
    }

    public void setButtonCallback(Consumer<BufferedImage> callback) {
        this.callback = callback;
    }

    public void setImage(@Nullable BufferedImage image) {
        this.image = image;
    }
}
