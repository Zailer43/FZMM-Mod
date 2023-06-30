package fzmm.zailer.me.compat.symbolChat.symbol;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.compat.CompatMods;
import fzmm.zailer.me.compat.symbolChat.SymbolChatCompat;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.replaceitem.symbolchat.SymbolChat;
import net.replaceitem.symbolchat.gui.SymbolSelectionPanel;

public class SymbolSelectionPanelComponentAdapter extends BaseComponent {

    private final SymbolSelectionPanel selectionPanel;
    private final SymbolChatCompat symbolChatCompat;

    public SymbolSelectionPanelComponentAdapter(SymbolSelectionPanel symbolSelectionPanel, SymbolChatCompat symbolChatCompat) {
        super();
        this.selectionPanel = symbolSelectionPanel;
        this.symbolChatCompat = symbolChatCompat;

        this.mouseDown().subscribe((mouseX, mouseY, button) ->
                this.symbolChatCompat.getSelectionPanelVisible() && this.selectionPanel.mouseClicked(mouseX, mouseY, button));

        this.mouseScroll().subscribe((mouseX, mouseY, amount) ->
                this.symbolChatCompat.getSelectionPanelVisible() && this.selectionPanel.mouseScrolled(mouseX, mouseY, amount));

        this.keyPress().subscribe(this.selectionPanel::keyPressed);
        this.charTyped().subscribe(this.selectionPanel::charTyped);
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
    protected int determineHorizontalContentSize(Sizing sizing) {
        return CompatMods.SYMBOL_CHAT_PRESENT ? SymbolSelectionPanel.WIDTH : 100;
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return CompatMods.SYMBOL_CHAT_PRESENT ? SymbolChat.config.getSymbolPanelHeight() : 150;
    }

    @Override
    public boolean isInBoundingBox(double x, double y) {
        if (!this.symbolChatCompat.getSelectionPanelVisible())
            return false;
        return super.isInBoundingBox(x, y);
    }
}
