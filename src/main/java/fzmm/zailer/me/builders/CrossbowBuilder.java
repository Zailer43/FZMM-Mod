package fzmm.zailer.me.builders;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class CrossbowBuilder {

    private final NbtCompound nbt;
    private final NbtList chargedProjectiles;

    private CrossbowBuilder() {
        this.nbt = new NbtCompound();
        this.chargedProjectiles = new NbtList();
    }

    public static CrossbowBuilder builder() {
        return new CrossbowBuilder();
    }

    public CrossbowBuilder setCharged(boolean charged) {
        this.nbt.putBoolean("Charged", charged);
        return this;
    }

    public CrossbowBuilder putProjectile(ItemStack projectile) {
        this.chargedProjectiles.add(projectile.writeNbt(new NbtCompound()));
        return this;
    }

    public ItemStack get() {
        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        this.nbt.put("ChargedProjectiles", this.chargedProjectiles);
        crossbow.setNbt(nbt);
        return crossbow;
    }

}
