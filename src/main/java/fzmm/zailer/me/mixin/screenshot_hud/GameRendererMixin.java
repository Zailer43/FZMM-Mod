package fzmm.zailer.me.mixin.screenshot_hud;

import fzmm.zailer.me.client.gui.components.image.source.ScreenshotSource;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "renderHand(FZLorg/joml/Matrix4f;)V", at = @At("HEAD"), cancellable = true)
    private void fzmm$removeHandInScreenshotHud(float tickProgress, boolean sleeping, Matrix4f positionMatrix, CallbackInfo ci) {
        if (ScreenshotSource.hasInstance()) {
            ci.cancel();
        }
    }
}
