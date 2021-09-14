package fzmm.zailer.me.utils;

import fzmm.zailer.me.config.FzmmConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
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
        addLoreToList(loreList, new LiteralText(message).setStyle(Style.EMPTY.withColor(messageColor)));
    }

    public static void addLoreToList(NbtList loreList, Text message) {
        loreList.add(NbtString.of(
                Text.Serializer.toJson(
                        FzmmUtils.disableItalicConfig(message)
                )
        ));
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
        lore.add(NbtString.of(Text.Serializer.toJson(text)));

        display.put(ItemStack.LORE_KEY, lore);
        return display;
    }

    public static void addLoreList(ItemStack stack, NbtList loreList) {
        NbtCompound tag = new NbtCompound();
        NbtCompound display = new NbtCompound();
        NbtList lore = new NbtList();

        if (stack.hasNbt()) {
            tag = stack.getNbt();
            assert tag != null;

            if (tag.contains(ItemStack.DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) {
                lore = tag.getCompound(ItemStack.DISPLAY_KEY).getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
                display.putString(ItemStack.NAME_KEY, tag.getCompound(ItemStack.DISPLAY_KEY).getString(ItemStack.NAME_KEY));
            }
        }

        lore.addAll(loreList);
        display.put(ItemStack.LORE_KEY, lore);
        tag.put(ItemStack.DISPLAY_KEY, display);
        stack.setNbt(tag);
    }

    public static void addLore(ItemStack stack, Text text) {
        NbtCompound tag = new NbtCompound();
        NbtCompound display = new NbtCompound();
        NbtList lore = new NbtList();

        if (stack.hasNbt()) {
            tag = stack.getNbt();
            assert tag != null;

            if (tag.contains(ItemStack.DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) {
                lore = tag.getCompound(ItemStack.DISPLAY_KEY).getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
                display.putString(ItemStack.NAME_KEY, tag.getCompound(ItemStack.DISPLAY_KEY).getString(ItemStack.NAME_KEY));
            }
        }

        addLoreToList(lore, text);
        display.put(ItemStack.LORE_KEY, lore);
        tag.put(ItemStack.DISPLAY_KEY, display);
        stack.setNbt(tag);
    }
}
