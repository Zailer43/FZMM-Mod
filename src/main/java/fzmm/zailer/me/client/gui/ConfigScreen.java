package fzmm.zailer.me.client.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.Configs;
import fzmm.zailer.me.config.hotkeys.Hotkeys;

import java.util.List;

public class ConfigScreen extends GuiConfigsBase {
    private static ConfigGuiTab tab = ConfigGuiTab.GENERIC;

    public ConfigScreen() {
        super(10, 50, FzmmClient.MOD_ID, null, "fzmm.gui.title.configs");
    }

    @Override
    public void initGui() {
        super.initGui();
        this.clearOptions();

        int x = 10;
        int y = 26;

        x += this.createButton(x, y, ConfigGuiTab.GENERIC);
        x += this.createButton(x, y, ConfigGuiTab.ENCODEBOOK);
        x += this.createButton(x, y, ConfigGuiTab.COLORS);
        this.createButton(x, y, ConfigGuiTab.HOTKEYS);
    }

    private int createButton(int x, int y, ConfigGuiTab tab) {
        ButtonGeneric button = new ButtonGeneric(x, y, -1, 20, tab.getDisplayName());
        button.setEnabled(ConfigScreen.tab != tab);
        this.addButton(button, new ButtonListener(tab, this));

        return button.getWidth() + 2;
    }


    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        List<? extends IConfigBase> configs;

        configs = switch (tab) {
            case GENERIC -> Configs.Generic.OPTIONS;
            case ENCODEBOOK -> Configs.Encodebook.OPTIONS;
            case COLORS -> Configs.Colors.OPTIONS;
            case HOTKEYS -> Hotkeys.HOTKEY_LIST;
        };

        return ConfigOptionWrapper.createFor(configs);
    }

    private record ButtonListener(ConfigGuiTab tab, ConfigScreen parent) implements IButtonActionListener {

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            ConfigScreen.tab = this.tab;

            this.parent.reCreateListWidget();
            this.parent.getListWidget().resetScrollbarPosition();
            this.parent.initGui();
        }
    }

    private enum ConfigGuiTab {
        GENERIC("generic"),
        ENCODEBOOK("encodebook"),
        COLORS("colors"),
        HOTKEYS("hotkeys");

        static final String BASE_KEY = "fzmm.gui.button.configGui.";

        private final String translationKey;

        ConfigGuiTab(String translationKey) {
            this.translationKey = BASE_KEY +translationKey;
        }

        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
        }
    }
}