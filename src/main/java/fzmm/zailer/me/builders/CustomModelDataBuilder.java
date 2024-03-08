package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CustomModelDataBuilder {
    private ItemStack stack;

    private CustomModelDataBuilder() {
        this.stack = Items.STONE.getDefaultStack();
    }

    public static CustomModelDataBuilder builder() {
        return new CustomModelDataBuilder();
    }

    public CustomModelDataBuilder of(ItemStack stack) {
        this.stack = stack.copy();
        return this;
    }

    public ItemStack get() {
        return this.stack;
    }

    public Optional<Integer> value() {
        if (!this.stack.hasNbt())
            return Optional.empty();

        NbtCompound nbtCompound = this.stack.getNbt();
        assert nbtCompound != null;

        return nbtCompound.contains(TagsConstant.CUSTOM_MODEL_DATA, NbtElement.INT_TYPE) ?
                Optional.of(nbtCompound.getInt(TagsConstant.CUSTOM_MODEL_DATA)) : Optional.empty();
    }

    public CustomModelDataBuilder value(@Nullable Integer value) {
        NbtCompound nbtCompound = this.stack.getOrCreateNbt();
        if (value == null)
            nbtCompound.remove(TagsConstant.CUSTOM_MODEL_DATA);
        else
            nbtCompound.putInt(TagsConstant.CUSTOM_MODEL_DATA, value);

        return this;
    }

    public CustomModelDataBuilder item(Item item) {
        ItemStack newStack = new ItemStack(item);
        newStack.setNbt(this.stack.getNbt());
        newStack.setCount(Math.max(1, this.stack.getCount()));

        this.stack = newStack;
        return this;
    }
}
