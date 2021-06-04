package fzmm.zailer.me.client.keys;

import fzmm.zailer.me.config.FzmmConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class TextObfuscatedKey {

    public static KeyBinding textObfuscated;

    public static void init() {
        textObfuscated = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.textObfuscated",
                GLFW.GLFW_KEY_O,
                "key.categories.fzmm"
        ));
    }

    public static void handleInputEvents() {
        if (textObfuscated.wasPressed()) {
            FzmmConfig config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();
            config.general.textObfuscated = !config.general.textObfuscated;
            AutoConfig.getConfigHolder(FzmmConfig.class).save();
        }
    }
}
