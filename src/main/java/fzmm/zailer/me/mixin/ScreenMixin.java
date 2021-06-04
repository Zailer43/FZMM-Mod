package fzmm.zailer.me.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ScreenMixin {
	@Shadow @Nullable protected MinecraftClient client;
	private int previousKey = 0;
	private static boolean active = false;
	private static short previousScale;

	@Inject(method = "keyPressed", at = @At("HEAD"))
	public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		assert this.client != null;

		if (previousKey == 341 && keyCode == 47) { // 341 = CTRL   47 = -
			if (!active) {
				previousScale = (short) this.client.options.guiScale;
				this.client.options.guiScale = 1;
			} else {
				this.client.options.guiScale = previousScale;
			}
			active = !active;
			this.client.onResolutionChanged();

		}
		previousKey = keyCode;
	}
}
