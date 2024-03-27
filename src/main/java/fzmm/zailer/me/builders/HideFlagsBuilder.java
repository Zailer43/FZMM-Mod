package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

public class HideFlagsBuilder {

    private ItemStack stack;

    private HideFlagsBuilder() {
        this.stack = Items.STONE.getDefaultStack();
    }

    public static HideFlagsBuilder builder() {
        return new HideFlagsBuilder();
    }

    public HideFlagsBuilder of(ItemStack stack) {
        this.stack = stack.copy();
        return this;
    }

    public ItemStack get() {
        NbtCompound nbt = this.stack.getOrCreateNbt();

        if (this.hideFlags() == 0)
            nbt.remove(TagsConstant.HIDE_FLAGS);

        if (nbt.isEmpty())
            nbt = null;

        this.stack.setNbt(nbt);

        return this.stack;
    }

    public int hideFlags() {
        return this.stack.getOrCreateNbt().getInt(TagsConstant.HIDE_FLAGS);
    }

    public HideFlagsBuilder hideFlags(int flags) {
        this.stack.getOrCreateNbt().putInt(TagsConstant.HIDE_FLAGS, flags);
        return this;
    }

    public boolean has(ItemStack.TooltipSection flag) {
        return (this.hideFlags() & flag.getFlag()) != 0;
    }

    public HideFlagsBuilder set(ItemStack.TooltipSection flag) {
        int flags = this.hideFlags();
        flags |= flag.getFlag();
        return this.hideFlags(flags);
    }

    public HideFlagsBuilder remove(ItemStack.TooltipSection flag) {
        int flags = this.hideFlags();
        flags &= ~flag.getFlag();
        return this.hideFlags(flags);
    }

    public HideFlagsBuilder setAll(boolean value) {
        int result = 0;
        if (value) {
            for (var flag : ItemStack.TooltipSection.values())
                result |= flag.getFlag();
        }

        return this.hideFlags(result);
    }
}
