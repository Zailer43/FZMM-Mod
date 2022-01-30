package fzmm.zailer.me.mixin;

import fzmm.zailer.me.client.PickItem;
import fzmm.zailer.me.client.gui.AbstractFzmmScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Shadow
	public ClientPlayerEntity player;

	@Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;doItemPick()V"))
	private void doItemPick(MinecraftClient client) {
		PickItem.doItemPick(client);
	}

	@Inject(method = "setScreen", at = @At("HEAD"))
	public void setScreen(Screen screen, CallbackInfo ci) {
		if (screen instanceof AbstractFzmmScreen && !AbstractFzmmScreen.previousScreen.contains(screen)) {
			AbstractFzmmScreen.previousScreen.add(screen);
		}
	}
}
