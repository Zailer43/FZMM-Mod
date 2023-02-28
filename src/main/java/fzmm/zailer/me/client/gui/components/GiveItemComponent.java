package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;

public class GiveItemComponent extends ItemComponent {
    public GiveItemComponent(ItemStack stack) {
        super(stack);
        this.showOverlay(true);

        MinecraftClient client = MinecraftClient.getInstance();
        this.tooltip(stack.getTooltip(
                client.player,
                client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC
        ));

        this.cursorStyle(CursorStyle.HAND);
        this.mouseDown().subscribe((mouseX, mouseY, button) -> {
            FzmmUtils.giveItem(this.stack);
            UISounds.playButtonSound();
            return true;
        });
    }
}
