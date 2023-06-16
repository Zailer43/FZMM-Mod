package fzmm.zailer.me.client.toast;

import fzmm.zailer.me.client.toast.status.IStatus;
import fzmm.zailer.me.client.toast.status.ImageStatus;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;

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
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        if (this.isLoaded && this.loadedTime == -1)
            this.loadedTime = startTime;

        super.draw(context, manager, startTime);

        return this.isLoaded && (startTime - this.loadedTime) > 3000 ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public IStatus getStatus() {
        return this.status;
    }

    @Override
    public Text getDetails() {
        return this.status.getDetailsTranslation();
    }

    public void setResponse(ImageStatus status) {
        this.isLoaded = true;
        this.status = status;
    }
}
