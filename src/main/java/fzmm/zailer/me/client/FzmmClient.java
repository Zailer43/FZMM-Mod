package fzmm.zailer.me.client;

import fzmm.zailer.me.client.gui.FzmmMainScreen;
import fzmm.zailer.me.config.FzmmConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;


@Environment(EnvType.CLIENT)
public class FzmmClient implements ClientModInitializer {

    public final static String MOD_ID = "fzmm";
    public final static Logger LOGGER = LogManager.getLogger("FZMM");
    public static final FzmmConfig CONFIG = FzmmConfig.createAndLoad();
    public static final KeyBinding OPEN_MAIN_GUI_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding("text.config.fzmm.option.hotkeys.fzmmMainGui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.category.fzmm"));


    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(FzmmCommand::registerCommands);
        FzmmItemGroup.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (OPEN_MAIN_GUI_KEYBINDING.wasPressed()) {
                client.setScreen(new FzmmMainScreen(client.currentScreen));
            }
        });
    }
}