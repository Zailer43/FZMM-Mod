package fzmm.zailer.me.mixin.screenshotHud;

import fzmm.zailer.me.client.gui.components.image.source.ScreenshotSource;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V", at = @At("HEAD"), cancellable = true)
    private void fzmm$removeHandInScreenshotHud(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo ci) {
        if (ScreenshotSource.hasInstance())
            ci.cancel();
    }
}
