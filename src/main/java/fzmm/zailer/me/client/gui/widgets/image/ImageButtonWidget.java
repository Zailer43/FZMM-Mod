package fzmm.zailer.me.client.gui.widgets.image;

import fzmm.zailer.me.client.gui.widgets.image.source.IImageSource;
import fzmm.zailer.me.client.gui.widgets.image.source.ImagePlayerNameSource;
import fzmm.zailer.me.client.toast.LoadingImageToast;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;

public class ImageButtonWidget extends ButtonWidget {

    private BufferedImage image;
    private IImageSource mode;
    private PressAction onValueChanged;

    public ImageButtonWidget() {
        super(0, 0, 0, 0, Text.empty(), button -> {});
        this.verticalSizing(Sizing.fixed(20));
        this.image = null;
        this.mode = new ImagePlayerNameSource();
        this.onValueChanged = null;
    }

    @Nullable
    public BufferedImage getImage() {
        return this.image;
    }

    public void setSourceType(IImageSource mode) {
        this.mode = mode;
    }

    public boolean hasNoImage() {
        return this.image == null;
    }

    public void loadImage(String value) {
        new Thread(() -> {
            LoadingImageToast toast = new LoadingImageToast();
            MinecraftClient.getInstance().getToastManager().add(toast);
            toast.setResponse(this.mode.loadImage(value));
            this.image = this.mode.getImage();
            this.onValueChanged();
        }).start();
    }

    public void onValueChanged() {
        if (this.onValueChanged != null)
            this.onValueChanged.onPress(this);
    }

    public void setOnValueChanged(PressAction pressAction) {
        this.onValueChanged = pressAction;
    }


//    public int getModeWidth() {
//        int width = 0;
//
//        for (var imageMode : ImageModeOption.values())
//            width = Math.max(width, imageMode.getWidth());
//
//        return width + 8;
//    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}
