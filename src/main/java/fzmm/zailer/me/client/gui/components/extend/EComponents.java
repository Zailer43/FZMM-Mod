package fzmm.zailer.me.client.gui.components.extend;

import fzmm.zailer.me.client.gui.components.extend.component.*;
import fzmm.zailer.me.utils.ItemUtils;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Extended components of the owo-lib components
 */
public class EComponents {
    public static EButtonComponent button(Component text) {
        return new EButtonComponent(text, buttonComponent -> {});
    }

    public static EItemComponent item(ItemStack stack) {
        return new EItemComponent(stack);
    }

    public static ELabelComponent label(Component text) {
        return new ELabelComponent(text);
    }

    public static EItemComponent itemGive(ItemStack stack) {
        EItemComponent result = new EItemComponent(stack);
        result.showOverlay(true)
                .setTooltipFromStack(true)
                .cursorStyle(CursorStyle.HAND)
                .mouseDown().subscribe((input, doubled) -> {
                    ItemUtils.give(stack);
                    UISounds.playButtonSound();
                    return true;
                });

        return result;
    }

    public static <E extends LivingEntity> EEntityComponent<E> entity(Sizing sizing, E entity) {
        return new EEntityComponent<>(sizing, entity);
    }

    public static ETextureComponent texture(Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        return new ETextureComponent(texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }
}
