package fzmm.zailer.me.client.gui.components.extend;

import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.component.EItemComponent;
import fzmm.zailer.me.client.gui.components.extend.component.ELabelComponent;
import fzmm.zailer.me.client.gui.components.extend.component.ETextureComponent;
import fzmm.zailer.me.utils.ItemUtils;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Extended components of the owo-lib components
 */
public class EComponents {
    public static EButtonComponent button(Text text) {
        return new EButtonComponent(text, buttonComponent -> {});
    }

    public static EItemComponent item(ItemStack stack) {
        return new EItemComponent(stack);
    }

    public static ELabelComponent label(Text text) {
        return new ELabelComponent(text);
    }

    public static EItemComponent itemGive(ItemStack stack) {
        EItemComponent result = new EItemComponent(stack);
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

    public static ETextureComponent texture(Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        return new ETextureComponent(texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }
}
