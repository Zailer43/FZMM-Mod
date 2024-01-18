package fzmm.zailer.me.mixin;

import fzmm.zailer.me.client.gui.utils.autoplacer.AutoPlacerHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class AutoPlacerMixin {

    @Shadow
    public abstract Text getName();

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean hasNbt();

    @Inject(method = "useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", at = @At("HEAD"), cancellable = true)
    public void fzmm$openPlayerStatuePlaceScreen(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {

        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        if (client.player.isSneaking() || !client.player.isCreative() || !this.hasNbt() && client.currentScreen != null) {
            return;
        }


        ItemStack stack = (ItemStack) ((Object) this);
        if (AutoPlacerHud.check(stack)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

}
