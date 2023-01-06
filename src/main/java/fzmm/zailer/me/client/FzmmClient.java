package fzmm.zailer.me.client;

import fzmm.zailer.me.client.gui.components.image.source.ScreenshotSource;
import fzmm.zailer.me.client.gui.main.MainScreen;
import fzmm.zailer.me.client.logic.FzmmHistory;
import fzmm.zailer.me.config.FzmmConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;


@Environment(EnvType.CLIENT)
public class FzmmClient implements ClientModInitializer {

    public final static String MOD_ID = "fzmm";
    public final static Logger LOGGER = LogManager.getLogger("FZMM");
    public static final FzmmConfig CONFIG = FzmmConfig.createAndLoad();
    public static final KeyBinding OPEN_MAIN_GUI_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fzmm.mainGui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.category.fzmm"));
    public static final int CHAT_BASE_COLOR = 0x478e47;
    public static final int CHAT_WHITE_COLOR = 0xb7b7b7;
    public static final boolean SYMBOL_CHAT_PRESENT = FabricLoader.getInstance().isModLoaded("symbol-chat");


    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(FzmmCommand::registerCommands);
        FzmmItemGroup.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!OPEN_MAIN_GUI_KEYBINDING.wasPressed())
                return;

            if (ScreenshotSource.hasInstance()) {
                ScreenshotSource.getInstance().takeScreenshot();
            } else {
                client.setScreen(new MainScreen(client.currentScreen));
            }
        });


        FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(container -> ResourceManagerHelper.registerBuiltinResourcePack(
                        new Identifier(MOD_ID, "fzmm_default_heads"),
                        container,
                        Text.literal("FZMM: Head generator"),
                        ResourcePackActivationType.DEFAULT_ENABLED
                )).filter(success -> !success).ifPresent(success -> LOGGER.warn("Could not register built-in resource pack with custom name."));

        CONFIG.history.subscribeToMaxItemHistory(integer -> FzmmHistory.update());
        CONFIG.history.subscribeToMaxHeadHistory(integer -> FzmmHistory.update());
    }
}