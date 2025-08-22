package fzmm.zailer.me.client.logic;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.WeakHashMap;

public class ItemTooltipAppend {
    private static final WeakHashMap<Integer, Long> compoundHash = WeakHashMap.newWeakHashMap(50);

    public static void init() {
        ItemTooltipCallback.EVENT.register(ItemTooltipAppend::addNbtLength);
    }


    private static void addNbtLength(ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> lines) {
        if (!FzmmClient.CONFIG.general.showItemSize() || !type.isAdvanced()) return;
        long stackSize;

        int hash = stack.getComponents().hashCode();
        if (compoundHash.containsKey(hash)) {
            stackSize = compoundHash.get(hash);
        } else {
            stackSize = ItemUtils.getLengthInBytes(stack);
            compoundHash.put(hash, stackSize);
        }

        MutableText text;
        if (stackSize > 1023) {
            text = Text.translatable("fzmm.item.tooltip.size.kilobytes", ItemUtils.getLengthInKB(stackSize));
        } else {
            text = Text.translatable("fzmm.item.tooltip.size.bytes", stackSize);
        }
        text = text.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));

        lines.add(text);
    }
}
