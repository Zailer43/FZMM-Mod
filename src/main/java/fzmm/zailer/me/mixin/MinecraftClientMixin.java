package fzmm.zailer.me.mixin;

import fzmm.zailer.me.client.PickItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Shadow
	public ClientPlayerEntity player;

	@Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;doItemPick()V"))
	private void doItemPick(MinecraftClient client) {
		PickItem.doItemPick(client);
	}

}
