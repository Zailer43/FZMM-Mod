//package fzmm.zailer.me.config.hotkeys;
//
//import fi.dy.masa.malilib.gui.GuiBase;
//import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
//import fi.dy.masa.malilib.hotkeys.IKeybind;
//import fi.dy.masa.malilib.hotkeys.KeyAction;
//import fzmm.zailer.me.client.gui.*;
//import fzmm.zailer.me.client.gui.headgenerator.HeadGeneratorScreen;
//import net.minecraft.client.gui.screen.Screen;
//
//public class OpenGuiCallback implements IHotkeyCallback {
//    private static final OpenGuiCallback instance = new OpenGuiCallback();
//
//    private OpenGuiCallback() {
//    }
//
//    public static OpenGuiCallback getInstance() {
//        return instance;
//    }
//
//    @Override
//    public boolean onKeyAction(KeyAction action, IKeybind key) {
//
//        Screen screen = null;
//        if (key == Hotkeys.FZMM_MAIN_GUI.getKeybind())
//            screen = new FzmmMainScreen(null);
//        else if (key == Hotkeys.CONFIG_GUI.getKeybind())
//            screen = new ConfigScreen(null);
//        else if (key == Hotkeys.CONVERTERS_GUI.getKeybind())
//            screen = new ConvertersScreen(null);
//        else if (key == Hotkeys.ENCODEBOOK_GUI.getKeybind())
//            screen = new EncryptbookScreen(null);
//        else if (key == Hotkeys.GRADIENT_GUI.getKeybind())
//            screen = new GradientScreen(null);
//        else if (key == Hotkeys.HEAD_GENERATOR_GUI.getKeybind())
//            screen = new HeadGeneratorScreen(null);
//        else if (key == Hotkeys.IMAGETEXT_GUI.getKeybind())
//            screen = new ImagetextScreen(null);
//        else if (key == Hotkeys.PLAYER_STATUE_GUI.getKeybind())
//            screen = new PlayerStatueScreen(null);
//
//        if (screen != null) {
//            GuiBase.openGui(screen);
//            return true;
//        }
//
//        return false;
//    }
//}
