package fzmm.zailer.me.config.hotkeys;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fzmm.zailer.me.client.gui.*;
import fzmm.zailer.me.client.gui.imagetext.*;
import fzmm.zailer.me.client.gui.playerStatue.StatueScreen;

public class OpenGuiCallback implements IHotkeyCallback {

    @Override
    public boolean onKeyAction(KeyAction action, IKeybind key) {

        if (key == Hotkeys.FZMM_MAIN_GUI.getKeybind()) {
            GuiBase.openGui(new FzmmMainScreen());
            return true;
        } else if (key == Hotkeys.CONFIG_GUI.getKeybind()) {
            GuiBase.openGui(new ConfigScreen());
            return true;
        } else if (key == Hotkeys.CONVERTERS_GUI.getKeybind()) {
            GuiBase.openGui(new ConvertersScreen());
            return true;
        } else if (key == Hotkeys.ENCODEBOOK_GUI.getKeybind()) {
            GuiBase.openGui(new EncodebookScreen());
            return true;
        } else if (key == Hotkeys.GRADIENT_GUI.getKeybind()) {
            GuiBase.openGui(new GradientScreen());
            return true;
        } else if (key == Hotkeys.IMAGETEXT_GUI.getKeybind()) {
            GuiBase.openGui(new ImagetextScreen());
            return true;
        } else if (key == Hotkeys.IMAGETEXT_BOOK_PAGE_GUI.getKeybind()) {
            GuiBase.openGui(new ImagetextBookPageScreen());
            return true;
        } else if (key == Hotkeys.IMAGETEXT_BOOK_TOOLTIP_GUI.getKeybind()) {
            GuiBase.openGui(new ImagetextBookTooltipScreen());
            return true;
        } else if (key == Hotkeys.IMAGETEXT_HOLOGRAM_GUI.getKeybind()) {
            GuiBase.openGui(new ImagetextHologramScreen());
            return true;
        } else if (key == Hotkeys.IMAGETEXT_LORE_GUI.getKeybind()) {
            GuiBase.openGui(new ImagetextLoreScreen());
            return true;
        } else if (key == Hotkeys.IMAGETEXT_TELLRAW_GUI.getKeybind()) {
            GuiBase.openGui(new ImagetextTellrawScreen());
            return true;
        } else if (key == Hotkeys.PLAYER_STATUE_GUI.getKeybind()) {
            GuiBase.openGui(new StatueScreen());
            return true;
        }

        return false;
    }
}
