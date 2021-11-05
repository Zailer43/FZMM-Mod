package fzmm.zailer.me.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class InventoryUtils {

    public static void addSlot(NbtList slotList, ItemStack stack, int slot) {
        slotList.add(getSlotTag(stack, slot));
    }

    public static NbtCompound getSlotTag(ItemStack stack, int slot) {
        NbtCompound slotTag = stackToTag(stack);
        slotTag.putByte("Slot", (byte) slot);
        return slotTag;
    }

    public static NbtCompound stackToTag(ItemStack stack) {
        NbtCompound tag = new NbtCompound();

        tag.putString("id", stack.getItem().toString());
        tag.putByte("Count", (byte) stack.getCount());

        if (stack.hasNbt())
            tag.put("tag", stack.getNbt());

        return tag;
    }
}
