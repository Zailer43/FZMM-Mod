package fzmm.zailer.me.mixin.item_group.operator_utilities_no_op;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {

    @Shadow
    @Final
    private boolean operatorTabEnabled;

    /**
     * Shows the operator utilities item-group even if user don't have op,
     * but only if it is enabled in the minecraft config
     */
    @ModifyReturnValue(method = "shouldShowOperatorTab(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("RETURN"))
    private boolean fzmm$showOperatorTabWithoutOp(boolean original) {
        if (this.operatorTabEnabled) return true;

        return original;
    }
}
