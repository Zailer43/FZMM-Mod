package fzmm.zailer.me.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NameTagItem.class)
public class NameTagItemMixin extends Item {

    public NameTagItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "useOnEntity", at = @At("HEAD"))
    public void useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {

        if (entity.getType().getName().getString().equals("Cat") && stack.getName().getString().equalsIgnoreCase("pelusa")) {
            ((CatEntity) entity).setCatType(5);
        }
    }
}

