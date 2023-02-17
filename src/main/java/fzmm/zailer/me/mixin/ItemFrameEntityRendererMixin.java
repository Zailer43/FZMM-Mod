package fzmm.zailer.me.mixin;

import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin<T extends ItemFrameEntity> {

    private boolean hasStack;

    @Inject(method = "render*", at = @At("HEAD"))
    private void render(T itemFrameEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        this.hasStack = itemFrameEntity.getHeldItemStack().isEmpty();
    }

    @ModifyVariable(method = "render*", at = @At("STORE"))
    private boolean disableItemFrameFrameRendering(boolean bl) {
        return (FzmmClient.CONFIG.general.forceInvisibleItemFrame() && !this.hasStack) || bl;
    }
}
