package fzmm.zailer.me.client.gui.components.image;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;

public class ScreenshotZoneComponent extends BaseComponent {
    public static final int PADDING = 25;

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        int smallerSide = Math.min(this.width, this.height);
        int halfLongerSide = smallerSide / 2;
        // The coordinates of the top left corner of the image
        int x = this.width / 2 - halfLongerSide;
        int y = this.height / 2 - halfLongerSide;

        int color = 0xC0101010;
        int bottomOfTopLine = y + PADDING;
        int topOfBottomLine = y + smallerSide - PADDING;

        //right
        context.fill(0, bottomOfTopLine, x + PADDING, topOfBottomLine, color);
        //left
        context.fill( x + smallerSide - PADDING, bottomOfTopLine, this.width, topOfBottomLine, color);
        //top
        context.fill( 0, 0, this.width, bottomOfTopLine, color);
        //bottom
        context.fill(0, topOfBottomLine, this.width, this.height, color);
    }
}
