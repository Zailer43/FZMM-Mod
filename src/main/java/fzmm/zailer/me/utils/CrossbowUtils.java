package fzmm.zailer.me.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class CrossbowUtils {

    private final NbtCompound nbt;
    private final NbtList chargedProjectiles;

    public CrossbowUtils() {
        this.nbt = new NbtCompound();
        this.chargedProjectiles = new NbtList();
    }

    public CrossbowUtils setCharged(boolean charged) {
        this.nbt.putBoolean("Charged", charged);
        return this;
    }

    public CrossbowUtils putProjectile(ItemStack projectile) {
        this.chargedProjectiles.add(InventoryUtils.stackToTag(projectile));
        return this;
    }

    public ItemStack get() {
        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        this.nbt.put("ChargedProjectiles", this.chargedProjectiles);
        crossbow.setNbt(nbt);
        return crossbow;
    }

}
