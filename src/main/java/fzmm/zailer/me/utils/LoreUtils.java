package fzmm.zailer.me.utils;

import fzmm.zailer.me.config.FzmmConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class LoreUtils {

    public static void setLore(ItemStack stack, NbtList lore) {
        NbtCompound display = new NbtCompound();

        if (stack.hasNbt()) {
            NbtCompound tag = stack.getNbt();
            assert tag != null;
            if (tag.contains(ItemStack.DISPLAY_KEY, NbtElement.COMPOUND_TYPE))
                display = tag.getCompound(ItemStack.DISPLAY_KEY);
        }
        assert display != null;

        display.put(ItemStack.LORE_KEY, lore);
        stack.setSubNbt(ItemStack.DISPLAY_KEY, display);
    }

    public static void addLoreToList(NbtList loreList, String message, int messageColor) {
        addLoreToList(loreList, getMessageWithColor(message, messageColor));
    }

    public static void addLoreToList(NbtList loreList, Text message) {
        loreList.add(FzmmUtils.textToNbtString(message, true));
    }

    public static NbtCompound generateLoreMessage(String message) {
        NbtCompound display = new NbtCompound();
        NbtList lore = new NbtList();
        String color = FzmmConfig.get().general.loreColorPickBlock;

        color = color.replaceAll("[^0-9A-Fa-f]]", "");
        if (color.length() != 6) {
            color = "19b2ff";
        }

        Text text = FzmmUtils.disableItalicConfig(new LiteralText(message).setStyle(
                Style.EMPTY.withColor(Integer.valueOf(color, 16))
        ));
        lore.add(FzmmUtils.textToNbtString(text, false));

        display.put(ItemStack.LORE_KEY, lore);
        return display;
    }

    public static void addLoreList(ItemStack stack, NbtList loreList) {
        NbtCompound tag = new NbtCompound();
        NbtCompound display = getDisplay(stack);
        NbtList lore = getLore(display);

        lore.addAll(loreList);
        display.put(ItemStack.LORE_KEY, lore);
        tag.put(ItemStack.DISPLAY_KEY, display);
        stack.setNbt(tag);
    }

    public static void addLore(ItemStack stack, Text text) {
        NbtCompound tag = new NbtCompound();
        NbtCompound display = getDisplay(stack);
        NbtList lore = getLore(display);

        addLoreToList(lore, text);
        display.put(ItemStack.LORE_KEY, lore);
        tag.put(ItemStack.DISPLAY_KEY, display);
        stack.setNbt(tag);
    }

    public static void addLore(ItemStack stack, String message, int messageColor) {
        addLore(stack, getMessageWithColor(message, messageColor));
    }

    private static NbtCompound getDisplay(ItemStack stack) {
        if (stack.hasNbt()) {
            NbtCompound nbt = stack.getNbt();
            assert nbt != null;

            if (nbt.contains(ItemStack.DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) {
                return nbt.getCompound(ItemStack.DISPLAY_KEY);
            }
        }
        return new NbtCompound();
    }

    private static NbtList getLore(NbtCompound display) {
        if (display.contains(ItemStack.LORE_KEY))
            return display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
        return new NbtList();
    }

    private static Text getMessageWithColor(String message, int color) {
        return new LiteralText(message).setStyle(Style.EMPTY.withColor(color));
    }
}
