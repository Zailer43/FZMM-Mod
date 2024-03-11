package fzmm.zailer.me.compat.symbolChat.symbol;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.compat.CompatMods;
import fzmm.zailer.me.compat.symbolChat.SymbolChatCompat;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.*;
import net.replaceitem.symbolchat.SymbolChat;
import net.replaceitem.symbolchat.gui.SymbolSelectionPanel;
import net.replaceitem.symbolchat.gui.widget.SymbolSearchBar;
import net.replaceitem.symbolchat.gui.widget.SymbolTabWidget;
import net.replaceitem.symbolchat.resource.SymbolTab;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SymbolSelectionPanelComponentAdapter extends BaseParentComponent {

    private final SymbolSelectionPanel selectionPanel;
    private SymbolSearchBar searchBar;
    private final SymbolChatCompat symbolChatCompat;

    @SuppressWarnings("unchecked")
    public SymbolSelectionPanelComponentAdapter(SymbolSelectionPanel symbolSelectionPanel, SymbolChatCompat symbolChatCompat) {
        super(Sizing.fixed(SymbolSelectionPanel.getWidthForTabs(8)), Sizing.fixed(SymbolChat.config.getSymbolPanelHeight()));
        this.selectionPanel = symbolSelectionPanel;
        this.symbolChatCompat = symbolChatCompat;

        this.mouseDown().subscribe((mouseX, mouseY, button) ->
                this.symbolChatCompat.isSelectionPanelVisible() && this.selectionPanel.mouseClicked(mouseX, mouseY, button));

        this.mouseScroll().subscribe((mouseX, mouseY, amount) ->
                this.symbolChatCompat.isSelectionPanelVisible() && this.selectionPanel.mouseScrolled(mouseX, mouseY, 0, amount));

        try {
            Field tabsField = this.selectionPanel.getClass().getDeclaredField("tabs");
            tabsField.setAccessible(true);
            List<SymbolTabWidget> tabs = (List<SymbolTabWidget>) tabsField.get(this.selectionPanel);

            for (var tabWidget : tabs) {
                Field tabField = SymbolTabWidget.class.getDeclaredField("tab");
                tabField.setAccessible(true);
                SymbolTab tab = (SymbolTab) tabField.get(tabWidget);

                if (tab.hasSearchBar()) {
                    Field searchField = SymbolTabWidget.class.getDeclaredField("searchBar");
                    searchField.setAccessible(true);
                    this.searchBar = (SymbolSearchBar) searchField.get(tabWidget);
                    break;
                }
            }
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolSelectionPanelComponentAdapter] Failed to get search bar field", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
        }
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT)
            return;

        try {
            this.selectionPanel.render(context, mouseX, mouseY, delta);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolSelectionPanelComponentAdapter] Failed to invoke render method", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
        }
    }

    @Override
    public boolean isInBoundingBox(double x, double y) {
        if (!this.symbolChatCompat.isSelectionPanelVisible())
            return false;
        return super.isInBoundingBox(x, y);
    }

    @Override
    public void layout(Size space) {

    }

    @Override
    public List<Component> children() {
        return new ArrayList<>();
    }

    @Override
    public ParentComponent removeChild(Component child) {
        return this;
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        FzmmClient.LOGGER.warn("[SymbolSelectionPanelComponentAdapter] " + keyCode);
        if (this.searchBar != null && this.searchBar.isFocused())
            return this.searchBar.onKeyPress(keyCode, scanCode, modifiers);

        return super.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onCharTyped(char chr, int modifiers) {
        if (this.searchBar != null && this.searchBar.isFocused())
            return this.searchBar.onCharTyped(chr, modifiers);

        return super.onCharTyped(chr, modifiers);
    }
}
