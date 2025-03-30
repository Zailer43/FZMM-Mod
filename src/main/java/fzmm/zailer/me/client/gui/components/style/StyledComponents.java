package fzmm.zailer.me.client.gui.components.style;

import fzmm.zailer.me.client.gui.components.style.component.StyledItemComponent;
import fzmm.zailer.me.client.gui.components.style.component.StyledLabelComponent;
import fzmm.zailer.me.utils.ItemUtils;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class StyledComponents {
    public static StyledLabelComponent label(Text text) {
        return new StyledLabelComponent(text);
    }

    public static StyledItemComponent item(ItemStack stack) {
        return new StyledItemComponent(stack);
    }

    public static StyledItemComponent itemGive(ItemStack stack) {
        StyledItemComponent result = new StyledItemComponent(stack);
        result.showOverlay(true)
                .setTooltipFromStack(true)
                .cursorStyle(CursorStyle.HAND)
                .mouseDown().subscribe((mouseX, mouseY, button) -> {
                    ItemUtils.give(stack);
                    UISounds.playButtonSound();
                    return true;
                });

        return result;
    }
}
