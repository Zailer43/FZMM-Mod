package fzmm.zailer.me.client.keys;

import fzmm.zailer.me.client.gui.FzmmMainScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class FzmmGuiKey {
	public static KeyBinding FzmmGuiKey;

	public static void init() {
		FzmmGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.FzmmOpenGui",
			GLFW.GLFW_KEY_Z,
			"key.categories.fzmm"
		));
	}

	public static void handleInputEvents() {
		if (FzmmGuiKey.wasPressed())
			MinecraftClient.getInstance().openScreen(new FzmmMainScreen());
	}
}
