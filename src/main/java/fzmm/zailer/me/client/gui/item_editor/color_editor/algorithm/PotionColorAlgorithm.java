package fzmm.zailer.me.client.gui.item_editor.color_editor.algorithm;

import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.NbtElement;
import net.minecraft.potion.PotionUtil;

public class PotionColorAlgorithm implements IColorAlgorithm{
    @Override
    public int getColor(ItemStack stack) {
        return stack.getOrCreateNbt().getInt(PotionUtil.CUSTOM_POTION_COLOR_KEY);
    }

    @Override
    public void setColor(ItemStack stack, int rgb) {
        stack.getOrCreateNbt().putInt(PotionUtil.CUSTOM_POTION_COLOR_KEY, rgb);
    }

    @Override
    public void removeTag(ItemStack stack) {
        stack.getOrCreateNbt().remove(PotionUtil.CUSTOM_POTION_COLOR_KEY);
    }

    @Override
    public boolean hasTag(ItemStack stack) {
        return stack.getOrCreateNbt().contains(PotionUtil.CUSTOM_POTION_COLOR_KEY, NbtElement.NUMBER_TYPE);
    }

    @Override
    public boolean isApplicable(ItemStack stack) {
        return stack.getItem() instanceof PotionItem;
    }

    @Override
    public String getId() {
        return "potion";
    }
}
