package fzmm.zailer.me.client.gui.components.image;

import fzmm.zailer.me.client.gui.components.image.source.IImageSource;
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
import java.util.function.Function;

public class ImageButtonWidget extends ButtonComponent {

    @Nullable
    private BufferedImage image;
    private IImageSource mode;
    private Function<BufferedImage, ImageStatus> imageLoadEvent;

    public ImageButtonWidget() {
        super(Text.empty(), button -> {});
        this.verticalSizing(Sizing.fixed(20));
        this.image = null;
        this.mode = new ImagePlayerNameSource();
        this.imageLoadEvent = null;
    }

    public Optional<BufferedImage> getImage() {
        return Optional.ofNullable(this.image);
    }

    public void setSourceType(IImageSource mode) {
        this.mode = mode;
    }

    public boolean hasImage() {
        return this.image != null;
    }

    public void loadImage(String value) {
        new Thread(() -> {
            this.active = false;
            LoadingImageToast toast = new LoadingImageToast();
            MinecraftClient.getInstance().getToastManager().add(toast);
            ImageStatus status = this.mode.loadImage(value);

            if (this.imageLoadEvent != null && status.statusType() == ImageStatus.StatusType.SUCCESSFUL)
                status = this.imageLoadEvent.apply(this.mode.getImage());

            this.active = true;
            toast.setResponse(status);

            this.image = this.mode.getImage();
        }).start();
    }

    /**
     * @param callback The event will only fire if the image is loaded successfully,
     * the callback return will overwrite the status
     */
    public void setImageLoadedEvent(Function<BufferedImage, ImageStatus> callback) {
        this.imageLoadEvent = callback;
    }

    public void setImage(@Nullable BufferedImage image) {
        this.image = image;
    }
}
