package fzmm.zailer.me.utils;

import fi.dy.masa.malilib.util.Color4f;
import fzmm.zailer.me.config.Configs;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class BlockStateTagItem {

    private final Item item;
    private final NbtCompound blockStateTag;
    private final String itemName;

    public BlockStateTagItem(Item item, String itemName) {
        this.item = item;
        this.blockStateTag = new NbtCompound();
        this.itemName = itemName;
    }

    public BlockStateTagItem(Item item) {
        this.item = item;
        this.blockStateTag = new NbtCompound();
        this.itemName = null;
    }

    public ItemStack get() {
        DisplayUtils displayUtils = new DisplayUtils(this.item);
        if (this.itemName != null) {
            Color4f color = Configs.Colors.USEFUL_BLOCK_STATES.getColor();
            displayUtils.setName(this.itemName, color).addLore("Place me!", color);
        }
        ItemStack stack = displayUtils.get();

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
