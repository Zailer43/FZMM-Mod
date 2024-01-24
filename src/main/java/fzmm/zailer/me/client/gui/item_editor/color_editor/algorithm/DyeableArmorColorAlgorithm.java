package fzmm.zailer.me.client.gui.item_editor.color_editor.algorithm;

import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class DyeableArmorColorAlgorithm implements IColorAlgorithm {

    @Override
    public int getColor(ItemStack stack) {
        return stack.getOrCreateNbt().getCompound(ItemStack.DISPLAY_KEY).getInt(DyeableArmorItem.COLOR_KEY);
    }

    @Override
    public void setColor(ItemStack stack, int rgb) {
        NbtCompound display = stack.getOrCreateNbt().getCompound(ItemStack.DISPLAY_KEY);
        display.putInt(DyeableArmorItem.COLOR_KEY, rgb);
        stack.setSubNbt(ItemStack.DISPLAY_KEY, display);
    }

    @Override
    public void removeTag(ItemStack stack) {
        stack.getOrCreateNbt().getCompound(ItemStack.DISPLAY_KEY).remove(DyeableArmorItem.COLOR_KEY);
    }

    @Override
    public boolean isApplicable(ItemStack stack) {
        return stack.getItem() instanceof DyeableArmorItem;
    }

    @Override
    public String getId() {
        return "leather";
    }
}
