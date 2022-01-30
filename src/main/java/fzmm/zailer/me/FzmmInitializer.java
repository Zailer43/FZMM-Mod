package fzmm.zailer.me;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.FzmmCommand;
import fzmm.zailer.me.client.FzmmItemGroup;
import fzmm.zailer.me.config.Configs;
import fzmm.zailer.me.config.hotkeys.HotkeyCallbacks;
import fzmm.zailer.me.config.hotkeys.InputHandler;

public class FzmmInitializer implements IInitializationHandler {

    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(FzmmClient.MOD_ID, new Configs());

        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerKeyboardInputHandler(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerMouseInputHandler(InputHandler.getInstance());

        HotkeyCallbacks.init();

        FzmmCommand.registerCommands();
        FzmmItemGroup.register();
    }
}
