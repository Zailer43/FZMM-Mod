package fzmm.zailer.me.config.hotkeys;

import fi.dy.masa.malilib.hotkeys.*;
import fzmm.zailer.me.client.FzmmClient;

public class InputHandler implements IKeybindProvider, IKeyboardInputHandler, IMouseInputHandler {
    private static final InputHandler INSTANCE = new InputHandler();

    public static InputHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void addKeysToMap(IKeybindManager manager) {
        for (IHotkey hotkey : Hotkeys.HOTKEY_LIST)
            manager.addKeybindToMap(hotkey.getKeybind());
    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(FzmmClient.MOD_ID, "fzmm.hotkeys.category.generic_hotkeys", Hotkeys.HOTKEY_LIST);
    }
}
