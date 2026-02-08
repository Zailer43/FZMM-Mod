package fzmm.zailer.me.mixin.item_group.operator_utilities_no_op;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {

    @Shadow
    @Final
    private boolean displayOperatorCreativeTab;

    /**
     * Shows the operator utilities item-group even if user don't have op,
     * but only if it is enabled in the minecraft config
     */
    @ModifyReturnValue(method = "hasPermissions(Lnet/minecraft/world/entity/player/Player;)Z", at = @At("RETURN"))
    private boolean fzmm$showOperatorTabWithoutOp(boolean original) {
        if (this.displayOperatorCreativeTab) return true;

        return original;
    }
}
