package fzmm.zailer.me.mixin;

import fzmm.zailer.me.client.TextObfuscated;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    public void handleInputEvents(CallbackInfo ci) {
        TextObfuscated.handleInputEvents();
    }
}