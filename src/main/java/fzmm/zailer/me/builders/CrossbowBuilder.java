package fzmm.zailer.me.builders;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;

import java.util.ArrayList;
import java.util.List;

public class CrossbowBuilder {

    private final ItemStack stack;
    private final List<ItemStack> chargedProjectiles;

    private CrossbowBuilder() {
        this.stack = Items.CROSSBOW.getDefaultInstance();
        this.chargedProjectiles = new ArrayList<>();
    }

    public static CrossbowBuilder builder() {
        return new CrossbowBuilder();
    }

    public CrossbowBuilder putProjectile(ItemStack projectile) {
        this.chargedProjectiles.add(projectile);
        return this;
    }

    public ItemStack get() {
        this.stack.update(DataComponents.CHARGED_PROJECTILES, null,
                component -> ChargedProjectiles.of(new ArrayList<>(this.chargedProjectiles)));
        return stack.copy();
    }

}
