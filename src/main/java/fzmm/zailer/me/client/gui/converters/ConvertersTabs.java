package fzmm.zailer.me.client.gui.converters;

import fzmm.zailer.me.client.gui.IScreenTab;
import fzmm.zailer.me.client.gui.converters.tabs.ConverterArrayToUuidTab;
import fzmm.zailer.me.client.gui.converters.tabs.ConverterBase64Tab;
import fzmm.zailer.me.client.gui.converters.tabs.ConverterUuidToArrayTab;
import io.wispforest.owo.ui.container.FlowLayout;

public enum ConvertersTabs implements IScreenTab {
    BASE64(new ConverterBase64Tab()),
    UUID_TO_ARRAY(new ConverterUuidToArrayTab()),
    ARRAY_TO_UUID(new ConverterArrayToUuidTab());

    private final IScreenTab tab;

    ConvertersTabs(IScreenTab tab) {
        this.tab = tab;
    }

    @Override
    public String getId() {
        return this.tab.getId();
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.tab.setupComponents(rootComponent);
    }
}
