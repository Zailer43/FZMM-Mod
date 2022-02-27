package fzmm.zailer.me.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fzmm.zailer.me.client.gui.ConfigScreen;

public class ModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (screen) -> new ConfigScreen(null);
    }
}