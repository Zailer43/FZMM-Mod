package fzmm.zailer.me.mixin.screenshotHud;

import fzmm.zailer.me.client.gui.components.image.source.ScreenshotSource;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void fzmm$removeHandInScreenshotHud(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (ScreenshotSource.hasInstance())
            ci.cancel();
    }
}
