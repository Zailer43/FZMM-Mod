package fzmm.zailer.me.client.gui.converters.tabs;

import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;

import java.util.function.Supplier;

public enum ConvertersTabs implements ITabsEnum {
    BASE64(ConverterBase64Tab::new),
    UUID_TO_ARRAY(ConverterUuidToArrayTab::new),
    ARRAY_TO_UUID(ConverterArrayToUuidTab::new);

    private final Supplier<IScreenTab> tabSupplier;
    private final String id;

    ConvertersTabs(Supplier<IScreenTab> tabSupplier) {
        this.tabSupplier = tabSupplier;
        this.id = this.createTab().getId();
    }

    @Override
    public IScreenTab createTab() {
        return this.tabSupplier.get();
    }

    @Override
    public String getId() {
        return this.id;
    }
}
