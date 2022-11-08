package fzmm.zailer.me.client.toast;

import fzmm.zailer.me.client.toast.status.IStatus;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public abstract class AbstractStatusToast implements Toast {
    private static final int LINE_DISTANCE = 12;

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        IStatus status = this.getStatus();

        Drawer.fill(matrices, 0, 0, this.getWidth(), this.getHeight(), status.getBackgroundColor());
        Drawer.drawRectOutline(matrices, 0, 0, this.getWidth(), this.getHeight(), status.getOutlineColor());

        int xOffset = 40;

        status.getIcon().render(matrices, 12, 12, 0, 0, 0);
        manager.getClient().textRenderer.drawWithShadow(matrices, status.getStatusTranslation(), xOffset, 8, 0xFFFFFF);
        List<OrderedText> detailsLines = this.getDetailsLines();
        for (int i = 0; i != detailsLines.size(); i++)
            manager.getClient().textRenderer.drawWithShadow(matrices, detailsLines.get(i), xOffset, 8 + LINE_DISTANCE + i * LINE_DISTANCE, 0xFFFFFF);
        return startTime > 3000 ? Visibility.HIDE : Visibility.SHOW;
    }

    public abstract IStatus getStatus();

    @Override
    public int getWidth() {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int titleWidth = textRenderer.getWidth(this.getStatus().getStatusTranslation());

        int detailsWidth = 0;
        List<OrderedText> detailsLines = this.getDetailsLines();
        for (var detailsLine : detailsLines)
            detailsWidth = Math.max(detailsWidth, textRenderer.getWidth(detailsLine));

        int maxWidth = Math.max(titleWidth, detailsWidth);
        // 44 = 4 padding + 32 icon + 4 padding + text + 4 padding
        return 44 + maxWidth;
    }

    @Override
    public int getHeight() {
        // 40 = 4 padding + 32 icon + 4 padding - 16;
        int height = 26 + this.getDetailsLines().size() * LINE_DISTANCE;

        // 40 = 4 padding + 32 icon + 4 padding
        return Math.max(40, height);
    }

    public abstract Text getDetails();

    protected List<OrderedText> getDetailsLines() {
        return MinecraftClient.getInstance().textRenderer.wrapLines(this.getDetails(), 500);
    }
}
