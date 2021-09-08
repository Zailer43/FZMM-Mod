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

import java.util.ArrayList;

public class LoreUtils {
    public static NbtCompound createMultipleLore(ItemStack itemStack, ArrayList<NbtString> loreArray) {
        NbtCompound tag = new NbtCompound();
        NbtCompound display = new NbtCompound();
        NbtList lore;

        if (itemStack.getNbt() == null) {
            display.put(ItemStack.LORE_KEY, null);
            tag.put(ItemStack.DISPLAY_KEY, display);
            itemStack.setNbt(tag);
        }

        tag = itemStack.getNbt();
        lore = tag.getCompound(ItemStack.DISPLAY_KEY).getList(ItemStack.LORE_KEY, 8);
        lore.addAll(loreArray);
        display.put(ItemStack.LORE_KEY, lore);
        display.putString(ItemStack.NAME_KEY, tag.getCompound(ItemStack.DISPLAY_KEY).getString(ItemStack.NAME_KEY));
        tag.put(ItemStack.DISPLAY_KEY, display);

        return tag;
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

    public static void addLore(ItemStack stack, Text text) {

        //{display:{Lore:['{"text":"1"}','{"text":"2"}','[{"text":"3"},{"text":"4"}]']}}

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
