package fzmm.zailer.me.client.gui.components.extend.component;

import io.wispforest.owo.ui.component.ItemComponent;
import net.minecraft.world.item.ItemStack;

/**
 * Add lazy-loading of tooltips, because generate a grid of items with tooltip is expensive
 **/
public class EItemComponent extends ItemComponent {
    private boolean lazyTooltip = false;
    private boolean lazyTooltipNeedsUpdate = true;

    public EItemComponent(ItemStack stack) {
        super(stack);

        this.mouseEnter().subscribe(this::ladyLoadTooltip);
    }

    @Override
    public ItemComponent setTooltipFromStack(boolean setTooltipFromStack) {
        this.lazyTooltip = setTooltipFromStack;
        this.lazyTooltipNeedsUpdate = true;
        return super.setTooltipFromStack(false);
    }

    @Override
    public boolean setTooltipFromStack() {
        return this.lazyTooltip;
    }

    private void ladyLoadTooltip() {
        if (this.lazyTooltip && this.lazyTooltipNeedsUpdate) {
            this.setTooltipFromStack = true;
            this.updateTooltipForStack(this.stack());
            this.setTooltipFromStack = false;
            this.lazyTooltipNeedsUpdate = false;
        }
    }

    @Override
    protected void updateTooltipForStack() {
        this.lazyTooltipNeedsUpdate = true;
    }

    protected void updateTooltipForStack(ItemStack stack) {
        super.updateTooltipForStack();
    }
}