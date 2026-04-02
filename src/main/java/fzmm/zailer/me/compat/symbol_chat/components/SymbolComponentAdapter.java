package fzmm.zailer.me.compat.symbol_chat.components;

import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.replaceitem.symbolchat.SymbolChat;
import net.replaceitem.symbolchat.SymbolInsertable;
import net.replaceitem.symbolchat.gui.SymbolSelectionPanel;

public class SymbolComponentAdapter extends VanillaWidgetComponent {
    protected final SymbolSelectionPanel widget;

    private SymbolComponentAdapter(SymbolSelectionPanel widget) {
        super(widget);
        this.widget = widget;
        this.widget.visible = true;

        // fix click in the background
        this.mouseDown().subscribe((input, doubled) -> true);
    }

    public static SymbolComponentAdapter of(SymbolInsertable writeConsumer) {
        return new SymbolComponentAdapter(new SymbolSelectionPanel(0, 0, SymbolChat.getConfig().symbolPanelHeight.get(), writeConsumer));
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        // fix scroll with smooth as it depends on the render
        // It is not being called because the custom implementation
        // of NonScrollableContainerWidget in Symbol Chat is not compatible with owo-lib by default
        this.widget.extractRenderState(graphics, mouseX, mouseY, delta);
    }


}
