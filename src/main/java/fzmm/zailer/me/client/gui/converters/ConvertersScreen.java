package fzmm.zailer.me.client.gui.converters;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.ScreenTabRow;
import fzmm.zailer.me.client.gui.converters.tabs.ConvertersTabs;
import io.wispforest.owo.ui.component.ButtonComponent;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

public class ConvertersScreen extends BaseFzmmScreen {
    private static ConvertersTabs selectedTab = ConvertersTabs.BASE64;

    public ConvertersScreen(@Nullable Screen parent) {
        super("converters", "converters", parent);
    }

    @Override
    protected void setup(EFlowLayout rootComponent) {
        this.setTabs(selectedTab);
        ScreenTabRow.setup(rootComponent, "tabs", selectedTab);
        for (var converterTab : ConvertersTabs.values()) {
            IScreenTab tab = this.getTab(converterTab, IScreenTab.class);
            tab.setupComponents(rootComponent);
            ButtonComponent button = rootComponent.childByIdOrThrow(ButtonComponent.class, ScreenTabRow.getScreenTabButtonId(tab.getId()));
            button.active(!tab.getId().equals(selectedTab.getId()));
            button.onPress(buttonComponent -> selectedTab = this.selectScreenTab(rootComponent, tab, selectedTab));
        }
        this.selectScreenTab(rootComponent, selectedTab, selectedTab);
    }
}