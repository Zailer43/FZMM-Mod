package fzmm.zailer.me.mixin.entity.force_invisible_item_frame;

import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameRenderer.class)
public class ItemFrameRendererMixin<T extends ItemFrame> {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ItemFrame;Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;F)V",
            at = @At("RETURN")
    )
    private void fzmm$disableItemFrameFrameRendering(T itemFrameEntity, ItemFrameRenderState state, float f, CallbackInfo ci) {
        if ((FzmmClient.CONFIG.general.forceInvisibleItemFrame() && !state.item.isEmpty())) {
            state.isInvisible = true;
            state.frameModel.clear();
        }
    }

}
