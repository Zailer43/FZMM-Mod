package fzmm.zailer.me.client.gui.converters;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

public class ConvertersScreen extends BaseFzmmScreen {
    private static ConvertersTabs selectedTab = ConvertersTabs.BASE64;

    public ConvertersScreen(@Nullable Screen parent) {
        super("converters", "converters", parent);
    }

    @Override
    protected void tryAddComponentList(FlowLayout rootComponent) {
        this.tryAddComponentList(rootComponent, "converters-options-list",
                this.newScreenTabRow(selectedTab)
        );

        FlowLayout container = rootComponent.childById(FlowLayout.class, "converters-options-list");
        if (container == null)
            return;

        for (var tab : ConvertersTabs.values())
            container.child(this.newScreenTab(tab.getId(), tab.getComponents(this)));
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        for (var tab : ConvertersTabs.values()) {
            tab.setupComponents(this, rootComponent);
            this.setupButton(rootComponent, this.getScreenTabButtonId(tab), tab != selectedTab, button -> {
                this.selectScreenTab(rootComponent, tab);
                selectedTab = tab;
            });
        }
        this.selectScreenTab(rootComponent, selectedTab);
    }
}