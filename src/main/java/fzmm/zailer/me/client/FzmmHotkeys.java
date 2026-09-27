package fzmm.zailer.me.client;

import com.mojang.blaze3d.platform.InputConstants;
import fzmm.zailer.me.client.gui.components.image.source.ScreenshotSource;
import fzmm.zailer.me.client.gui.main.MainScreen;
import fzmm.zailer.me.client.gui.utils.auto_placer.AutoPlacerHud;
import fzmm.zailer.me.utils.FzmmUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class FzmmHotkeys {
    private static KeyMapping OPEN_MAIN_GUI;
    private static KeyMapping OPEN_CONTEXT_MENU;

    public static void register() {
        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "general"));
        OPEN_MAIN_GUI = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fzmm.mainGui", InputConstants.KEY_Z, category));
        OPEN_CONTEXT_MENU = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fzmm.contextMenu", InputConstants.UNKNOWN.getValue(), category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!OPEN_MAIN_GUI.consumeClick()) return;

            if (ScreenshotSource.hasInstance()) {
                ScreenshotSource.getInstance().takeScreenshot();
            } else if (AutoPlacerHud.isHudActive) {
                AutoPlacerHud.removeHud();
            } else {
                FzmmUtils.setScreen(new MainScreen(client.gui.screen()));
            }
        });
    }

    public static KeyMapping mainGuiKey() {
        return OPEN_MAIN_GUI;
    }

    public static KeyMapping contextMenuKey() {
        return OPEN_CONTEXT_MENU;
    }
}
