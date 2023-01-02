package fzmm.zailer.me.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin {


    @Inject(method = "shouldDisplay()Z", at = @At("HEAD"), cancellable = true)
    private void noDisplayOperatorItemGroup(CallbackInfoReturnable<Boolean> cir) {
        if (((Object) this) == ItemGroups.OPERATOR)
            cir.setReturnValue(MinecraftClient.getInstance().options.getOperatorItemsTab().getValue());
    }
}
