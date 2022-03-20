package fzmm.zailer.me.config.hotkeys;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fzmm.zailer.me.config.Configs;
import fzmm.zailer.me.utils.DisplayUtils;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.InventoryUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericCallback implements IHotkeyCallback {
    private static final GenericCallback instance = new GenericCallback();

    private GenericCallback() {
    }

    public static GenericCallback getInstance() {
        return instance;
    }

    @Override
    public boolean onKeyAction(KeyAction action, IKeybind key) {
        boolean returnValue = false;

        if (key == Hotkeys.INCREMENT_GUI_SCALE.getKeybind())
            returnValue = this.changeGuiScale(true);
        else if (key == Hotkeys.DECREMENT_GUI_SCALE.getKeybind())
            returnValue = this.changeGuiScale(false);
        else if (key == Hotkeys.COPY_ITEM_NAME.getKeybind())
            returnValue = this.copyItemName(false);
        else if (key == Hotkeys.COPY_ITEM_NAME_JSON.getKeybind())
            returnValue = this.copyItemName(true);
        else if (key == Hotkeys.GIVE_IN_ITEM_FRAME.getKeybind())
            returnValue = this.giveInItemFrame();
        else if (key == Hotkeys.PASTE_IN_SLOT.getKeybind())
            returnValue = this.pasteInSlot();

        return returnValue;
    }

    private boolean changeGuiScale(boolean increment) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int guiScale = mc.options.guiScale;

        if (increment)
            guiScale++;
        else
            guiScale--;

        guiScale = MathHelper.clamp(guiScale, 1, 4);

        mc.options.guiScale = guiScale;
        mc.onResolutionChanged();
        return true;
    }

    private boolean copyItemName(boolean copyStyle) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ItemStack stack = InventoryUtils.getFocusedItem();
        if (stack == null)
            return true;

        Text name = stack.hasCustomName() ? stack.getName() : stack.getItem().getName();

        if (copyStyle) {
            mc.keyboard.setClipboard(Text.Serializer.toJson(name));
        } else {
            mc.keyboard.setClipboard(name.getString());
        }
        return true;
    }

    private boolean giveInItemFrame() {
        ItemStack stack = InventoryUtils.getFocusedItem();
        if (stack == null)
            return true;

        String stackNameStr = new DisplayUtils(stack).getName();
        DisplayUtils itemFrame = new DisplayUtils(InventoryUtils.getInItemFrame(stack, false))
                .addLore("Item: " + stack.getItem().toString(), Configs.Colors.ITEM_FRAME_HOTKEY.getColor());

        if (stackNameStr != null)
            itemFrame.setName(Text.Serializer.fromJson(stackNameStr));

        FzmmUtils.giveItem(itemFrame.get());
        return true;
    }

    private boolean pasteInSlot() {
        Slot slot = InventoryUtils.getFocusedSlot();
        if (slot == null)
            return true;

        ItemStack slotStack = slot.getStack();
        String clipboard = MinecraftClient.getInstance().keyboard.getClipboard();

        if (slot.hasStack()) {
            DisplayUtils display = new DisplayUtils(slotStack).addLore(clipboard.split("\r\n"));

            FzmmUtils.giveItem(display.get());
        } else {
            Pattern giveRegex = Pattern.compile("^(/|)(fzmm give|give [a-zA-Z0-9_@]{2,16}(\\[.*]|)) ([a-zA-Z0-9:_]{1,255})(\\{.*}|)( [0-9]{1,2}|)$");
            Matcher matcher = giveRegex.matcher(clipboard);

            if (!matcher.find())
                return true;

            try {
                ItemStack stack = Registry.ITEM.get(new Identifier(matcher.group(4))).getDefaultStack();

                if (!matcher.group(5).isEmpty()) {
                    NbtCompound nbt = NbtHelper.fromNbtProviderString(matcher.group(5));
                    stack.setNbt(nbt);
                }

                if (!matcher.group(6).isEmpty()) {
                    int count = Integer.parseInt(matcher.group(6).strip());
                    stack.setCount(count);
                }

                FzmmUtils.giveItem(stack);
            } catch (Exception ignored) {
            }
        }

        return true;
    }
}