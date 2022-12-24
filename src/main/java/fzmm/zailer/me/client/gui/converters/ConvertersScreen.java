package fzmm.zailer.me.client.gui.converters;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
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
        ScreenTabRow.setup(rootComponent, "tabs", selectedTab);
        for (var tab : ConvertersTabs.values()) {
            tab.setupComponents(rootComponent);
            ButtonRow.setup(rootComponent, ScreenTabRow.getScreenTabButtonId(tab), tab != selectedTab, button -> {
                this.selectScreenTab(rootComponent, tab);
                selectedTab = tab;
            });
        }
        this.selectScreenTab(rootComponent, selectedTab);
    }
}