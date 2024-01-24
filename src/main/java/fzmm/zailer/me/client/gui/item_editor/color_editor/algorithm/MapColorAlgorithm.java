package fzmm.zailer.me.client.gui.item_editor.color_editor.algorithm;

import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class MapColorAlgorithm implements IColorAlgorithm {
    @Override
    public int getColor(ItemStack stack) {
        return stack.getOrCreateNbt().getCompound(ItemStack.DISPLAY_KEY).getInt(TagsConstant.DISPLAY_MAP_COLOR);
    }

    @Override
    public void setColor(ItemStack stack, int rgb) {
        NbtCompound display = stack.getOrCreateNbt().getCompound(ItemStack.DISPLAY_KEY);
        display.putInt(TagsConstant.DISPLAY_MAP_COLOR, rgb);
        stack.setSubNbt(ItemStack.DISPLAY_KEY, display);
    }

    @Override
    public void removeTag(ItemStack stack) {
        stack.getOrCreateNbt().getCompound(ItemStack.DISPLAY_KEY).remove(TagsConstant.DISPLAY_MAP_COLOR);
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
