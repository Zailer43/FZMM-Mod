package fzmm.zailer.me.client.gui.headgenerator;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;

public abstract class AbstractHeadList<T extends ElementListWidget.Entry<T>> extends ElementListWidget<T> {
    public AbstractHeadList(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
        this.setRenderBackground(false);
        this.setRenderHorizontalShadows(false);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        double d = this.client.getWindow().getScaleFactor();
        RenderSystem.enableScissor((int) ((double) this.getRowLeft() * d), (int) ((double) (this.height - this.bottom) * d), (int) ((double) (this.getScrollbarPositionX() + 6) * d), (int) ((double) (this.height - (this.height - this.bottom) - this.top - 4) * d));
        super.render(matrices, mouseX, mouseY, delta);
        RenderSystem.disableScissor();
    }
    @Override
    protected int getScrollbarPositionX() {
        return this.getRowLeft() + this.getRowWidth();
    }

    @Override
    public int getRowWidth() {
        return this.width - 50;
    }

    public int getLeft() {
        return this.left + (this.width - this.getRowWidth()) / 2 + 2;
    }
}