//package fzmm.zailer.me.config.hotkeys;
//
//import com.google.common.collect.ImmutableList;
//import fi.dy.masa.malilib.config.options.ConfigHotkey;
//import fi.dy.masa.malilib.hotkeys.IHotkey;
//import fi.dy.masa.malilib.hotkeys.KeyAction;
//import fi.dy.masa.malilib.hotkeys.KeybindSettings;
//
//public class Hotkeys {
//    private static final KeybindSettings ANY_CONTEXT = KeybindSettings.create(KeybindSettings.Context.ANY, KeyAction.PRESS, false, true, false, true);
//
//    public static final ConfigHotkey FZMM_MAIN_GUI = new ConfigHotkey("fzmmMainGui", "Z", KeybindSettings.RELEASE_EXCLUSIVE,  "");
//    public static final ConfigHotkey CONFIG_GUI = new ConfigHotkey("configGui", "Z,C",  "");
//    public static final ConfigHotkey CONVERTERS_GUI = new ConfigHotkey("convertersGui", "", "");
//    public static final ConfigHotkey ENCODEBOOK_GUI = new ConfigHotkey("encryptbook", "", "");
//    public static final ConfigHotkey GRADIENT_GUI = new ConfigHotkey("gradientGui", "Z,G", "");
//    public static final ConfigHotkey HEAD_GENERATOR_GUI = new ConfigHotkey("headGeneratorGui", "Z,H", "");
//    public static final ConfigHotkey IMAGETEXT_GUI = new ConfigHotkey("imagetextGui", "Z,I", "");
//    public static final ConfigHotkey PLAYER_STATUE_GUI = new ConfigHotkey("playerStatueGui", "Z,P", "");
//
//    public static final ConfigHotkey INCREMENT_GUI_SCALE = new ConfigHotkey("incrementGuiScale", "LEFT_CONTROL,KP_ADD", ANY_CONTEXT,"");
//    public static final ConfigHotkey DECREMENT_GUI_SCALE = new ConfigHotkey("decrementGuiScale", "LEFT_CONTROL,KP_SUBTRACT", ANY_CONTEXT, "");
//    public static final ConfigHotkey COPY_ITEM_NAME = new ConfigHotkey("copyItemName", "LEFT_CONTROL,C", KeybindSettings.MODIFIER_GUI, "");
//    public static final ConfigHotkey COPY_ITEM_NAME_JSON = new ConfigHotkey("copyItemNameJson", "LEFT_ALT,C", KeybindSettings.MODIFIER_GUI, "");
//    public static final ConfigHotkey GIVE_IN_ITEM_FRAME = new ConfigHotkey("giveInItemFrame", "LEFT_CONTROL,I", KeybindSettings.MODIFIER_GUI, "");
//    public static final ConfigHotkey PASTE_IN_SLOT = new ConfigHotkey("pasteInSlot", "LEFT_CONTROL,V", KeybindSettings.MODIFIER_GUI, "");
//    public static final ConfigHotkey GENERATE_IMAGETEXT = new ConfigHotkey("generateImagetext", "LEFT_CONTROL,F2", KeybindSettings.PRESS_ALLOWEXTRA, "");
//
//    public static final ImmutableList<IHotkey> HOTKEY_LIST = ImmutableList.of(
//            FZMM_MAIN_GUI,
//            CONFIG_GUI,
//            CONVERTERS_GUI,
//            ENCODEBOOK_GUI,
//            GRADIENT_GUI,
//            HEAD_GENERATOR_GUI,
//            IMAGETEXT_GUI,
//            PLAYER_STATUE_GUI,
//
//            INCREMENT_GUI_SCALE,
//            DECREMENT_GUI_SCALE,
//            COPY_ITEM_NAME,
//            COPY_ITEM_NAME_JSON,
//            GIVE_IN_ITEM_FRAME,
//            PASTE_IN_SLOT,
//            GENERATE_IMAGETEXT
//    );
//
//}