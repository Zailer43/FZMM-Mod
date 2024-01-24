package fzmm.zailer.me.client.gui.item_editor.color_editor.algorithm;

import net.minecraft.item.ItemStack;

public interface IColorAlgorithm {

    int getColor(ItemStack stack);

    void setColor(ItemStack stack, int rgb);

    void removeTag(ItemStack stack);

    boolean isApplicable(ItemStack stack);

    String getId();
}
