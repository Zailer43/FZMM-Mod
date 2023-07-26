package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.FzmmItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;

import java.util.Optional;

public class BlockStateItemBuilder {

    private Item item;
    private NbtCompound blockStateTag;
    private NbtCompound nbt;
    private String itemName;

    private BlockStateItemBuilder() {
        this.item = Items.AIR;
        this.blockStateTag = new NbtCompound();
        this.itemName = null;
    }

    public static BlockStateItemBuilder builder() {
        return new BlockStateItemBuilder();
    }

    public ItemStack get() {
        ItemStack stack = this.item.getDefaultStack();
        stack.setNbt(this.nbt);

        if (this.itemName != null) {
            DisplayBuilder displayBuilder = DisplayBuilder.of(stack);
            int color = FzmmClient.CONFIG.colors.usefulBlockStates().rgb();
            displayBuilder.setName(this.itemName, color)
                    .addLore(Text.translatable(FzmmItemGroup.USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY + ".place").getString(), color);
            stack = displayBuilder.get();
        }

        if (this.blockStateTag.isEmpty())
            stack.removeSubNbt(BlockItem.BLOCK_STATE_TAG_KEY);
        else
            stack.setSubNbt(BlockItem.BLOCK_STATE_TAG_KEY, this.blockStateTag);

        return stack;
    }

    public BlockStateItemBuilder of(ItemStack stack) {
        this.item(stack.getItem())
                .nbt(stack.getOrCreateNbt());
        return this;
    }

    public BlockStateItemBuilder itemName(Item translationItem, String translationKey) {
        this.item(translationItem);
        this.itemName = Text.translatable(FzmmItemGroup.USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY + ".item." + translationKey, translationItem.getName().getString()).getString();
        return this;
    }

    public BlockStateItemBuilder item(Item item) {
        this.item = item;
        return this;
    }

    public Item item() {
        return this.item;
    }

    public BlockStateItemBuilder nbt(NbtCompound nbt) {
        this.nbt = nbt;
        return this.blockStateTag(nbt.getCompound(BlockItem.BLOCK_STATE_TAG_KEY));
    }

    public NbtCompound nbt() {
        return this.nbt;
    }

    public BlockStateItemBuilder blockStateTag(NbtCompound blockStateTag) {
        this.blockStateTag = blockStateTag;
        return this;
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

    public BlockStateItemBuilder add(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            String key = property.getName();
            String value = state.get(property).toString().toLowerCase();
            this.blockStateTag.putString(key, value);
        }

        return this;
    }

    public BlockStateItemBuilder remove(String key) {
        this.blockStateTag.remove(key);
        return this;
    }

    public BlockStateItemBuilder clearStates() {
        this.blockStateTag = new NbtCompound();
        return this;
    }

    public boolean contains(String key) {
        return this.blockStateTag.contains(key, NbtElement.STRING_TYPE);
    }

    public boolean isState(String property, String value) {
        return this.blockStateTag.contains(property, NbtElement.STRING_TYPE) && this.blockStateTag.getString(property).equals(value);
    }

    public Optional<BlockState> blockState() {
        if (!(this.get().getItem() instanceof BlockItem blockItem))
            return Optional.empty();

        BlockState state = blockItem.getBlock().getDefaultState();
        StateManager<Block, BlockState> stateManager = state.getBlock().getStateManager();

        for (var propertyString : this.blockStateTag.getKeys()) {
            Property<?> property = stateManager.getProperty(propertyString);
            if (property != null && this.blockStateTag.contains(propertyString, NbtElement.STRING_TYPE)) {
                String valueString = this.blockStateTag.getString(propertyString);
                state = BlockItem.with(state, property, valueString);
            }
        }

        return Optional.of(state);
    }
}