package fzmm.zailer.me.client.toast;

import fzmm.zailer.me.client.gui.widgets.image.ImageStatus;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;

public class LoadingImageToast extends AbstractStatusToast {
    private IStatus status;
    private boolean isLoaded;
    private long loadedTime;

    public LoadingImageToast() {
        this.status = ImageStatus.LOADING;
        this.isLoaded = false;
        this.loadedTime = -1;
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        if (this.isLoaded && this.loadedTime == -1)
            this.loadedTime = startTime;

        super.draw(matrices, manager, startTime);

        return this.isLoaded && (startTime - this.loadedTime) > 3000 ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public IStatus getStatus() {
        return this.status;
    }

    public void setResponse(ImageStatus status) {
        this.isLoaded = true;
        this.status = status;
    }
}
