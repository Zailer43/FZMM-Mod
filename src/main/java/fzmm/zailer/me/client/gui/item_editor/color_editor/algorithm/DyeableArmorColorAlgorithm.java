package fzmm.zailer.me.client.gui.item_editor.color_editor.algorithm;

import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class DyeableArmorColorAlgorithm implements IColorAlgorithm {

    @Override
    public int getColor(ItemStack stack) {
        return this.getDisplay(stack).getInt(DyeableItem.COLOR_KEY);
    }

    @Override
    public void setColor(ItemStack stack, int rgb) {
        NbtCompound display = this.getDisplay(stack);
        display.putInt(DyeableItem.COLOR_KEY, rgb);
        stack.setSubNbt(ItemStack.DISPLAY_KEY, display);
    }

    @Override
    public void removeTag(ItemStack stack) {
        this.getDisplay(stack).remove(DyeableItem.COLOR_KEY);
    }

    @Override
    public boolean hasTag(ItemStack stack) {
        return this.getDisplay(stack).contains(DyeableItem.COLOR_KEY, NbtElement.NUMBER_TYPE);
    }

    private NbtCompound getDisplay(ItemStack stack) {
        return stack.getOrCreateNbt().getCompound(ItemStack.DISPLAY_KEY);
    }

    @Override
    public boolean isApplicable(ItemStack stack) {
        return stack.getItem() instanceof DyeableItem;
    }

    @Override
    public String getId() {
        return "leather";
    }
}
