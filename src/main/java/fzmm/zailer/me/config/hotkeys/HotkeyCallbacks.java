package fzmm.zailer.me.config.hotkeys;

public class HotkeyCallbacks {

    public static void init() {
        OpenGuiCallback openGuiCallback = OpenGuiCallback.getInstance();
        GenericCallback genericCallback = GenericCallback.getInstance();

        Hotkeys.FZMM_MAIN_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.CONFIG_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.CONVERTERS_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.ENCODEBOOK_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.GRADIENT_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.IMAGETEXT_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.PLAYER_STATUE_GUI.getKeybind().setCallback(openGuiCallback);

        Hotkeys.INCREMENT_GUI_SCALE.getKeybind().setCallback(genericCallback);
        Hotkeys.DECREMENT_GUI_SCALE.getKeybind().setCallback(genericCallback);
        Hotkeys.COPY_ITEM_NAME.getKeybind().setCallback(genericCallback);
        Hotkeys.COPY_ITEM_NAME_JSON.getKeybind().setCallback(genericCallback);
        Hotkeys.GIVE_IN_ITEM_FRAME.getKeybind().setCallback(genericCallback);
        Hotkeys.PASTE_IN_SLOT.getKeybind().setCallback(genericCallback);
        Hotkeys.GENERATE_IMAGETEXT.getKeybind().setCallback(genericCallback);
    }
}
