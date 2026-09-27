package fzmm.zailer.me.client.gui.components;

import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;

public class IconComponent extends BaseUIComponent {
    private final Icon icon;

    public IconComponent(Icon icon) {
        this.sizing(Sizing.fixed(16));
        this.icon = icon;
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        this.icon.render(graphics, this.x, this.y, this.width, this.height, delta);
    }
}
