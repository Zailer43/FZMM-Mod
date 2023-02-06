package fzmm.zailer.me.client.gui.converters;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.ScreenTabRow;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

public class ConvertersScreen extends BaseFzmmScreen {
    private static ConvertersTabs selectedTab = ConvertersTabs.BASE64;

    public ConvertersScreen(@Nullable Screen parent) {
        super("converters", "converters", parent);
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        this.setTabs(selectedTab);
        ScreenTabRow.setup(rootComponent, "tabs", selectedTab);
        for (var converterTab : ConvertersTabs.values()) {
            IScreenTab tab = this.getTab(converterTab, IScreenTab.class);
            tab.setupComponents(rootComponent);
            ButtonRow.setup(rootComponent, ScreenTabRow.getScreenTabButtonId(tab), !tab.getId().equals(selectedTab.getId()), button ->
                    selectedTab = this.selectScreenTab(rootComponent, tab, selectedTab));
        }
        this.selectScreenTab(rootComponent, selectedTab, selectedTab);
    }
}