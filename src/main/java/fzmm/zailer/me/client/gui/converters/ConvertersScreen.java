package fzmm.zailer.me.client.gui.converters;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.tabs.ITab;
import fzmm.zailer.me.client.gui.components.tabs.TabContainer;
import fzmm.zailer.me.client.gui.converters.tabs.ConverterArrayToUuidTab;
import fzmm.zailer.me.client.gui.converters.tabs.ConverterBase64Tab;
import fzmm.zailer.me.client.gui.converters.tabs.ConverterUuidToArrayTab;
import fzmm.zailer.me.client.logic.history.IMemento;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class ConvertersScreen extends BaseFzmmScreen implements IMemento {
    public static final String BUTTON_TRANSLATION_KEY = "fzmm.gui.converters.tab.";
    private TabContainer tabContainer;

    public ConvertersScreen(@Nullable Screen parent) {
        super("converters", "converters", parent);
    }

    @Override
    protected void setup(EFlowLayout rootComponent) {
        this.tabContainer = rootComponent.childByIdOrThrow(TabContainer.class, "tabs");
        List<ITab> tabs = List.of(new ConverterBase64Tab(), new ConverterUuidToArrayTab(), new ConverterArrayToUuidTab());
        this.tabContainer.addParsedTabs(tabs).setupTabs(rootComponent, tabs.get(0).getId());
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        this.tabContainer.backup(output);
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.tabContainer.restore(input);
    }
}