package fzmm.zailer.me.client.logic;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.WeakHashMap;

public class ItemTooltipAppend {
    private static final WeakHashMap<Integer, Long> compoundHash = WeakHashMap.newWeakHashMap(50);

    public static void init() {
        ItemTooltipCallback.EVENT.register(ItemTooltipAppend::addNbtLength);
    }


    private static void addNbtLength(ItemStack stack, Item.TooltipContext context, TooltipFlag type, List<Component> lines) {
        if (!FzmmClient.CONFIG.general.showItemSize() || !type.isAdvanced()) return;
        long stackSize;

        int hash = stack.getComponents().hashCode();
        if (compoundHash.containsKey(hash)) {
            stackSize = compoundHash.get(hash);
        } else {
            stackSize = ItemUtils.getLengthInBytes(stack);
            compoundHash.put(hash, stackSize);
        }

        MutableComponent text;
        if (stackSize > 1023) {
            text = Component.translatable("fzmm.item.tooltip.size.kilobytes", ItemUtils.getLengthInKB(stackSize));
        } else {
            text = Component.translatable("fzmm.item.tooltip.size.bytes", stackSize);
        }
        text = text.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY));

        lines.add(text);
    }
}
