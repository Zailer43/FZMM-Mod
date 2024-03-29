package fzmm.zailer.me.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin {

    // shows the operator utilities tab if it is enabled in the config,
    // this is to maintain compatibility with vanilla which is broken
    // by the changes to show the tab even if you don't have op
    @Inject(method = "shouldDisplay()Z", at = @At("HEAD"), cancellable = true)
    private void fzmm$noDisplayOperatorItemGroup(CallbackInfoReturnable<Boolean> cir) {
        if (((Object) this) == Registries.ITEM_GROUP.get(ItemGroups.OPERATOR))
            cir.setReturnValue(MinecraftClient.getInstance().options.getOperatorItemsTab().getValue());
    }

    @ModifyVariable(
            method = "updateEntries(Lnet/minecraft/item/ItemGroup$DisplayContext;)V",
            at = @At(value = "HEAD"),
            index = 1,
            argsOnly = true)
    public ItemGroup.DisplayContext fzmm$showOperatorUtilitiesWithoutOp(ItemGroup.DisplayContext value) {

        // displays the operator utilities item group even if you do not have op
        if (((Object) this) == Registries.ITEM_GROUP.get(ItemGroups.OPERATOR)) {
            value = new ItemGroup.DisplayContext(value.enabledFeatures(), true, value.lookup());
        }

        return value;
    }
}
