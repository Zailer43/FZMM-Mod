package fzmm.zailer.me.client.gui.components.image;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.util.math.MatrixStack;

public class ScreenshotZoneComponent extends BaseComponent {
    public static final int PADDING = 25;

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        int smallerSide = Math.min(this.width, this.height);
        int halfLongerSide = smallerSide / 2;
        // The coordinates of the top left corner of the image
        int x = this.width / 2 - halfLongerSide;
        int y = this.height / 2 - halfLongerSide;

        int color = 0xC0101010;
        int bottomOfTopLine = y + PADDING;
        int topOfBottomLine = y + smallerSide - PADDING;

        //right
        Drawer.fill(matrices, 0, bottomOfTopLine, x + PADDING, topOfBottomLine, color);
        //left
        Drawer.fill(matrices, x + smallerSide - PADDING, bottomOfTopLine, this.width, topOfBottomLine, color);
        //top
        Drawer.fill(matrices, 0, 0, this.width, bottomOfTopLine, color);
        //bottom
        Drawer.fill(matrices, 0, topOfBottomLine, this.width, this.height, color);
    }
}
