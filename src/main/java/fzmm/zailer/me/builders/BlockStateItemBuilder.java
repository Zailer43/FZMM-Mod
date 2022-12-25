package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.FzmmItemGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class BlockStateItemBuilder {

    private final Item item;
    private final NbtCompound blockStateTag;
    private final String itemName;

    public BlockStateItemBuilder(Item item, String itemNameTranslationKey) {
        this.item = item;
        this.blockStateTag = new NbtCompound();
        this.itemName = Text.translatable(FzmmItemGroup.USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY + "item." + itemNameTranslationKey).getString();
    }

    public BlockStateItemBuilder(Item item, String translationKey, Item translationItem) {
        this.item = item;
        this.blockStateTag = new NbtCompound();
        this.itemName = Text.translatable(FzmmItemGroup.USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY + "item." + translationKey, translationItem.getName().getString()).getString();
    }

    public BlockStateItemBuilder(Item item) {
        this.item = item;
        this.blockStateTag = new NbtCompound();
        this.itemName = null;
    }

    public ItemStack get() {
        DisplayBuilder displayBuilder = DisplayBuilder.builder().item(this.item);
        if (this.itemName != null) {
            int color = Integer.parseInt(FzmmClient.CONFIG.colors.usefulBlockStates(), 16);
            displayBuilder.setName(this.itemName, color)
                    .addLore(Text.translatable(FzmmItemGroup.USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY + "place").getString(), color);
        }
        ItemStack stack = displayBuilder.get();

        stack.setSubNbt(BlockItem.BLOCK_STATE_TAG_KEY, this.blockStateTag);
        return stack;
    }

    public BlockStateItemBuilder add(String key, String value) {
        this.blockStateTag.putString(key, value);
        return this;
    }

    public BlockStateItemBuilder add(String key, boolean value) {
        return this.add(key, String.valueOf(value));
    }

    public BlockStateItemBuilder add(String key, int value) {
        return this.add(key, String.valueOf(value));
    }
}
