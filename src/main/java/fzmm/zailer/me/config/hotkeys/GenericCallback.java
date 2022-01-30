package fzmm.zailer.me.config.hotkeys;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fzmm.zailer.me.client.gui.AbstractFzmmScreen;
import fzmm.zailer.me.mixin.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class GenericCallback implements IHotkeyCallback {

    @Override
    public boolean onKeyAction(KeyAction action, IKeybind key) {
        if (action != KeyAction.PRESS)
            return false;
        if (key == Hotkeys.INCREMENT_GUI_SCALE.getKeybind()) {
            this.changeGuiScale(true);
            return true;
        } else if (key == Hotkeys.DECREMENT_GUI_SCALE.getKeybind()) {
            this.changeGuiScale(false);
            return true;
        } else if (key == Hotkeys.COPY_ITEM_NAME.getKeybind()) {
            this.copyItemName(false);
            return true;
        } else if (key == Hotkeys.COPY_ITEM_NAME_JSON.getKeybind()) {
            this.copyItemName(true);
            return true;
        } else if (key == Hotkeys.BACK_IN_FZMM_GUI.getKeybind()) {
            AbstractFzmmScreen.previousScreen();
            return true;
        }

        return false;
    }

    private void changeGuiScale(boolean increment) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int guiScale = mc.options.guiScale;

        if (increment)
            guiScale++;
        else
            guiScale--;

        guiScale = MathHelper.clamp(guiScale, 1, 4);

        mc.options.guiScale = guiScale;
        mc.onResolutionChanged();
    }

    private void copyItemName(boolean copyStyle) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!(mc.currentScreen instanceof HandledScreen<?> screen))
            return;

        Slot slot = ((HandledScreenAccessor) screen).getFocusedSlot();
        if (slot == null)
            return;
        ItemStack stack = slot.getStack();

        Text name = stack.hasCustomName() ? stack.getName() : stack.getItem().getName();

        if (copyStyle) {
            mc.keyboard.setClipboard(Text.Serializer.toJson(name));
        } else {
            mc.keyboard.setClipboard(name.getString());
        }
    }
}