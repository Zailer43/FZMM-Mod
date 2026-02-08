package fzmm.zailer.me.mixin.screenshot_hud;

import fzmm.zailer.me.client.gui.components.image.source.ScreenshotSource;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IIIZZ)V", at = @At("HEAD"), cancellable = true)
    private void fzmm$removeChatInScreenshotHud(GuiGraphics graphics, Font font, int currentTick, int mouseX, int mouseY, boolean bl, boolean focused, CallbackInfo ci) {
        if (ScreenshotSource.hasInstance()) {
            ci.cancel();
        }
    }
}
