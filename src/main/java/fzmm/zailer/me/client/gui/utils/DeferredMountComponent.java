package fzmm.zailer.me.client.gui.utils;

import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.Sizing;

/**
 *  Component that defers mounting of another component until first update,
 *  removing itself and passing mouseX and mouseY
 */
public class DeferredMountComponent extends BaseUIComponent {
    private final Mount mouseMount;

    public DeferredMountComponent(Mount mouseMount) {
        super();
        this.sizing(Sizing.fill(1), Sizing.fixed(1));
        this.mouseMount = mouseMount;
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);
        if (this.parent == null) return;

        this.parent.removeChild(this);
        this.mouseMount.accept(this.parent, mouseX, mouseY);
    }

    @FunctionalInterface
    public interface Mount {
        void accept(ParentUIComponent parent, int mouseX, int mouseY);
    }
}
