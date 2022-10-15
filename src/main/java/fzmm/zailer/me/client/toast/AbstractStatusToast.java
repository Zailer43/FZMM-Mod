package fzmm.zailer.me.client.toast;

import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;

public abstract class AbstractStatusToast implements Toast {

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        IStatus status = this.getStatus();

        Drawer.fill(matrices, 0, 0, this.getWidth(), this.getHeight(), status.getBackgroundColor());
        Drawer.drawRectOutline(matrices, 0, 0, this.getWidth(), this.getHeight(), status.getOutlineColor());

        int xOffset = 40;

        status.getIcon().render(matrices, 12, 12, 0, 0, 0);
        manager.getClient().textRenderer.drawWithShadow(matrices, status.getStatusTranslation(), xOffset, 8, 0xFFFFFF);
        manager.getClient().textRenderer.drawWithShadow(matrices, status.getDetailsTranslation(), xOffset, 24, 0xFFFFFF);
        return startTime > 3000 ? Visibility.HIDE : Visibility.SHOW;
    }

    public abstract IStatus getStatus();

    @Override
    public int getWidth() {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int titleWidth = textRenderer.getWidth(this.getStatus().getStatusTranslation());
        int detailsWidth = textRenderer.getWidth(this.getStatus().getDetailsTranslation());
        int maxWidth = Math.max(titleWidth, detailsWidth);
        // 44 = 4 padding + 32 icon + 4 padding + text + 4 padding
        return 44 + maxWidth;
    }

    @Override
    public int getHeight() {
        return 40;
    }
}
