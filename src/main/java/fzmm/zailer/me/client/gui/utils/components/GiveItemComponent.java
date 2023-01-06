package fzmm.zailer.me.client.gui.utils.components;

import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class GiveItemComponent extends ItemComponent {
    public GiveItemComponent(ItemStack stack) {
        super(stack);
        this.showOverlay(true);

        MinecraftClient client = MinecraftClient.getInstance();
        this.tooltip(stack.getTooltip(
                client.player,
                client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC
        ));
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (this.hovered && GLFW.GLFW_MOUSE_BUTTON_1 == button) {
            FzmmUtils.giveItem(this.stack);
            UISounds.playButtonSound();
            return true;
        }

        return super.onMouseDown(mouseX, mouseY, button);
    }
}
