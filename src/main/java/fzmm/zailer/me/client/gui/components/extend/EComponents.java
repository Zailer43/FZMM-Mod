package fzmm.zailer.me.client.gui.components.extend;

import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.component.EEntityComponent;
import fzmm.zailer.me.client.gui.components.extend.component.EItemComponent;
import fzmm.zailer.me.client.gui.components.extend.component.ELabelComponent;
import fzmm.zailer.me.utils.ItemUtils;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

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

    public static <E extends LivingEntity> EEntityComponent<E> entity(Sizing sizing, E entity) {
        return new EEntityComponent<>(sizing, entity);
    }
}
