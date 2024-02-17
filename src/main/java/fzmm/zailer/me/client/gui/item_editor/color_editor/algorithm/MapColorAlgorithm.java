package fzmm.zailer.me.client.gui.item_editor.color_editor.algorithm;

import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class MapColorAlgorithm implements IColorAlgorithm {
    @Override
    public int getColor(ItemStack stack) {
        return this.getDisplay(stack).getInt(TagsConstant.DISPLAY_MAP_COLOR);
    }

    @Override
    public void setColor(ItemStack stack, int rgb) {
        NbtCompound display = this.getDisplay(stack);
        display.putInt(TagsConstant.DISPLAY_MAP_COLOR, rgb);
        stack.setSubNbt(ItemStack.DISPLAY_KEY, display);
    }

    @Override
    public void removeTag(ItemStack stack) {
        this.getDisplay(stack).remove(TagsConstant.DISPLAY_MAP_COLOR);
    }

    @Override
    public boolean hasTag(ItemStack stack) {
        return this.getDisplay(stack).contains(TagsConstant.DISPLAY_MAP_COLOR, NbtElement.NUMBER_TYPE);
    }

    private NbtCompound getDisplay(ItemStack stack) {
        return stack.getOrCreateNbt().getCompound(ItemStack.DISPLAY_KEY);
    }

    @Override
    public boolean isApplicable(ItemStack stack) {
        return stack.getItem() instanceof FilledMapItem;
    }

    @Override
    public String getId() {
        return "map";
    }
}
