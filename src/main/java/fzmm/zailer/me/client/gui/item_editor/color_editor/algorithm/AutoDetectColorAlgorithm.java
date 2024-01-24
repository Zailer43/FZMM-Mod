package fzmm.zailer.me.client.gui.item_editor.color_editor.algorithm;

import net.minecraft.item.ItemStack;

import java.util.List;

public class AutoDetectColorAlgorithm implements IColorAlgorithm {

    public static List<IColorAlgorithm> colorAlgorithms;
    public static IColorAlgorithm algorithm;

    @Override
    public int getColor(ItemStack stack) {
        for (var algorithm : colorAlgorithms) {
            if (algorithm.isApplicable(stack))
                return algorithm.getColor(stack);
        }
        return -1;
    }

    @Override
    public void setColor(ItemStack stack, int rgb) {
        for (var algorithm : colorAlgorithms) {
            if (algorithm.isApplicable(stack)) {
                algorithm.setColor(stack, rgb);
                return;
            }
        }
    }

    @Override
    public void removeTag(ItemStack stack) {
        for (var algorithm : colorAlgorithms) {
            if (algorithm.isApplicable(stack)) {
                algorithm.removeTag(stack);
                return;
            }
        }
    }

    @Override
    public boolean isApplicable(ItemStack stack) {
        for (var algorithm : colorAlgorithms) {
            if (algorithm.isApplicable(stack))
                return true;
        }
        return false;
    }

    @Override
    public String getId() {
        return "auto-detect";
    }

    static {

        colorAlgorithms = List.of(
                new DyeableArmorColorAlgorithm(),
                new MapColorAlgorithm(),
                new PotionColorAlgorithm()
        );

        algorithm = new AutoDetectColorAlgorithm();
    }
}
