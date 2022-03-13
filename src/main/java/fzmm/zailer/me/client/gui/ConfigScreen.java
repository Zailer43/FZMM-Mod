package fzmm.zailer.me.client.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import fi.dy.masa.malilib.util.StringUtils;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.Configs;
import fzmm.zailer.me.config.hotkeys.Hotkeys;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;

public class ConfigScreen extends GuiConfigsBase {
    private static ConfigGuiTab tab = ConfigGuiTab.GENERIC;

    public ConfigScreen(Screen parent) {
        super(10, 50, FzmmClient.MOD_ID, null, "fzmm.gui.title.configs");
        this.setParent(parent);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.clearOptions();

        int x = 10;
        int y = 26;

        x += this.createButton(x, y, ConfigGuiTab.GENERIC);
        x += this.createButton(x, y, ConfigGuiTab.ENCRYPTBOOK);
        x += this.createButton(x, y, ConfigGuiTab.COLORS);
        this.createButton(x, y, ConfigGuiTab.HOTKEYS);
    }

    private int createButton(int x, int y, ConfigGuiTab tab) {
        ButtonGeneric button = new ButtonGeneric(x, y, -1, ScreenConstants.NORMAL_BUTTON_HEIGHT, tab.getDisplayName());
        button.setEnabled(ConfigScreen.tab != tab);
        this.addButton(button, new ButtonListener(tab, this));

        return button.getWidth() + 2;
    }


    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        List<? extends IConfigBase> configs;

        configs = switch (tab) {
            case GENERIC -> Configs.Generic.OPTIONS;
            case ENCRYPTBOOK -> Configs.Encryptbook.OPTIONS;
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
            WidgetListConfigOptions listWidget = this.parent.getListWidget();
            assert listWidget != null;
            listWidget.resetScrollbarPosition();
            this.parent.initGui();
        }
    }

    private enum ConfigGuiTab {
        GENERIC("generic"),
        ENCRYPTBOOK("encryptbook"),
        COLORS("colors"),
        HOTKEYS("hotkeys");

        static final String BASE_KEY = "fzmm.gui.configGui.";

        private final String translationKey;

        ConfigGuiTab(String translationKey) {
            this.translationKey = BASE_KEY +translationKey;
        }

        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
        }
    }
}
