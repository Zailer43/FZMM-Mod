package fzmm.zailer.me.client.gui.components.image;

import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;

public class ScreenshotZoneComponent extends BaseUIComponent {
    public static final int PADDING = 25;

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        int smallerSide = Math.min(this.width, this.height);
        int halfLongerSide = smallerSide / 2;
        // The coordinates of the top left corner of the image
        int x = this.width / 2 - halfLongerSide;
        int y = this.height / 2 - halfLongerSide;

        int color = 0xC0101010;
        int bottomOfTopLine = y + PADDING;
        int topOfBottomLine = y + smallerSide - PADDING;

        //right
        graphics.fill(0, bottomOfTopLine, x + PADDING, topOfBottomLine, color);
        //left
        graphics.fill( x + smallerSide - PADDING, bottomOfTopLine, this.width, topOfBottomLine, color);
        //top
        graphics.fill( 0, 0, this.width, bottomOfTopLine, color);
        //bottom
        graphics.fill(0, topOfBottomLine, this.width, this.height, color);
    }
}
