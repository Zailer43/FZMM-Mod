package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

public class DurabilityBuilder {
    private ItemStack stack;

    private DurabilityBuilder() {
        this.stack = Items.GOLDEN_HOE.getDefaultStack();
    }

    public static DurabilityBuilder builder() {
        return new DurabilityBuilder();
    }

    public DurabilityBuilder of(ItemStack stack) {
        this.stack = stack.copy();
        return this;
    }

    public ItemStack get() {
        NbtCompound nbt = this.stack.getOrCreateNbt();

        if (this.damage() == 0)
            nbt.remove(ItemStack.DAMAGE_KEY);

        if (!this.unbreakable())
            nbt.remove(TagsConstant.UNBREAKABLE);

        if (nbt.isEmpty())
            nbt = null;

        this.stack.setNbt(nbt);

        return this.stack;
    }

    public DurabilityBuilder damage(int amount) {
        this.stack.getOrCreateNbt().putInt(ItemStack.DAMAGE_KEY, amount);
        return this;
    }

    public DurabilityBuilder doDamage(int amount) {
        this.stack.getOrCreateNbt().putInt(ItemStack.DAMAGE_KEY, Math.max(0, this.damage() + amount));
        return this;
    }

    public DurabilityBuilder doDamagePercentage(int percentage) {
        return this.doDamage((int) Math.abs(this.maxDamage() * percentage / 100.0d));
    }

    public DurabilityBuilder damagePercentage(double percentage) {
        return this.damage((int) Math.abs(this.maxDamage() * percentage / 100.0d));
    }

    public double getDamagePercentage() {
        int maxDamage = this.maxDamage();
        if (maxDamage == 0)
            return 0.0d;

        return this.damage() * 100.0d / this.maxDamage();
    }

    public double getDamagePercentage(int maxFloatingPoints) {
        double percentage = this.getDamagePercentage();

        double scaleFactor = Math.pow(10, maxFloatingPoints);

        return Math.round(percentage * scaleFactor) / scaleFactor;
    }

    public int damage() {
        return this.stack.getOrCreateNbt().getInt(ItemStack.DAMAGE_KEY);
    }

    public int maxDamage() {
        return this.stack.getMaxDamage();
    }

    public DurabilityBuilder unbreakable(boolean value) {
        this.stack.getOrCreateNbt().putBoolean(TagsConstant.UNBREAKABLE, value);
        return this;
    }

    public boolean unbreakable() {
        return this.stack.getOrCreateNbt().getBoolean(TagsConstant.UNBREAKABLE);
    }
}
