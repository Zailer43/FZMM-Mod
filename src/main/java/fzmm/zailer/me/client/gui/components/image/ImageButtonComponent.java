package fzmm.zailer.me.client.gui.components.image;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;
import fzmm.zailer.me.client.gui.components.image.source.IImageLoaderFromText;
import fzmm.zailer.me.client.gui.components.image.source.IInteractiveImageLoader;
import fzmm.zailer.me.client.gui.components.image.source.ImagePlayerNameSource;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.SnackBarBuilder;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class ImageButtonComponent extends ButtonComponent {

    private @Nullable BufferedImage image;
    private IImageGetter mode;
    private Function<BufferedImage, ImageStatus> imageLoadEvent;
    private Consumer<Optional<BufferedImage>> callback;

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
        if (this.mode instanceof IImageLoaderFromText imageLoaderFromText) {
            this.loadImageFromText(imageLoaderFromText, value);
        } else if (this.mode instanceof IInteractiveImageLoader interactiveImageLoader) {
            this.interactiveImageLoad(interactiveImageLoader);
        }
    }

    public void loadImageFromText(IImageLoaderFromText imageLoaderFromText, String value) {
        this.active = false;
        ISnackBarComponent loadingSnackBar = BaseSnackBarComponent.builder(SnackBarManager.IMAGE_ID)
                .title(Text.translatable("fzmm.snack_bar.image.loading.title"))
                .backgroundColor(FzmmStyles.ALERT_LOADING_COLOR)
                .keepOnLimit()
                .build();

        CompletableFuture.supplyAsync(() -> {
            MinecraftClient.getInstance().execute(() -> SnackBarManager.getInstance().add(loadingSnackBar));

            return imageLoaderFromText.loadImage(value);
        }).whenComplete((status, throwable) -> {
            Optional<BufferedImage> image = imageLoaderFromText.getImage();

            if (throwable != null) {
                FzmmClient.LOGGER.error("[ImageButtonComponent] Unexpected error loading an image", throwable);
                status = ImageStatus.UNEXPECTED_ERROR;
            }

            SnackBarBuilder snackBarStatus = BaseSnackBarComponent.builder(SnackBarManager.IMAGE_ID)
                    .title(status.getStatusTranslation())
                    .backgroundColor(status.getColor());

            if (status.isError() && status.hasDetails()) {
                snackBarStatus.highTimer()
                        .details(status.getDetailsTranslation())
                        .closeButton()
                        .keepOnLimit()
                        .expandDetails();
                FzmmClient.LOGGER.warn("[ImageButtonComponent] Failed to load image");
            } else {
                if (this.imageLoadEvent != null) {
                    assert image.isPresent();
                    this.imageLoadEvent.apply(image.get());
                }
                snackBarStatus.lowTimer();
                FzmmClient.LOGGER.info("[ImageButtonComponent] Image loaded successfully");
            }

            MinecraftClient.getInstance().execute(() -> {
                this.active = true;
                loadingSnackBar.close();
                SnackBarManager.getInstance().add(snackBarStatus.startTimer().build());
                if (this.image != null) {
                    this.image.flush();
                }

                this.image = image.orElse(null);

                if (this.callback != null) {
                    this.callback.accept(Optional.ofNullable(this.image));
                }
            });
        });
    }

    public void interactiveImageLoad(IInteractiveImageLoader interactiveImageLoader) {
        SnackBarManager.getInstance().remove(SnackBarManager.IMAGE_ID);

        this.active = false;
        interactiveImageLoader.execute(bufferedImage -> {
            if (this.image != null) {
                this.image.flush();
            }
            this.image = bufferedImage;

            this.active = true;

            if (this.callback != null) {
                this.callback.accept(Optional.ofNullable(this.image));
            }
        });
    }

    /**
     * @param callback The event will only fire if the image is loaded successfully,
     *                 the callback return will overwrite the status
     */
    public void setImageLoadedEvent(Function<BufferedImage, ImageStatus> callback) {
        this.imageLoadEvent = callback;
    }

    public void setButtonCallback(Consumer<Optional<BufferedImage>> callback) {
        this.callback = callback;
    }

    public void setImage(@Nullable BufferedImage image) {
        if (this.image != null) {
            this.image.flush();
        }
        this.image = image;
    }
}
