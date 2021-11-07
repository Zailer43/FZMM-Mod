package fzmm.zailer.me.utils;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;

public class BlockStateTagItem {

    private final ItemStack stack;
    private final NbtCompound blockStateTag;
    private final NbtString itemName;

    public BlockStateTagItem(Item item, String itemName) {
        this.stack = new ItemStack(item);
        this.blockStateTag = new NbtCompound();
        this.itemName = FzmmUtils.stringToNbtString(itemName, true);
    }

    public BlockStateTagItem(Item item) {
        this.stack = new ItemStack(item);
        this.blockStateTag = new NbtCompound();
        this.itemName = null;
    }

    public ItemStack get() {
        if (itemName != null) {
            NbtCompound display = new NbtCompound();
            display.put(ItemStack.NAME_KEY, this.itemName);
            stack.setSubNbt(ItemStack.DISPLAY_KEY, display);
        }

        stack.setSubNbt(BlockItem.BLOCK_STATE_TAG_KEY, this.blockStateTag);
        return stack;
    }

    public BlockStateTagItem add(String key, String value) {
        this.blockStateTag.putString(key, value);
        return this;
    }

    public BlockStateTagItem add(String key, boolean value) {
        return this.add(key, String.valueOf(value));
    }

    public BlockStateTagItem add(String key, int value) {
        return this.add(key, String.valueOf(value));
    }
}
