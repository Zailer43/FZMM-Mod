package fzmm.zailer.me.compat.symbol_chat.components;

import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.replaceitem.symbolchat.gui.SymbolSelectionPanel;

public class SymbolComponentAdapter extends VanillaWidgetComponent {
    protected final SymbolSelectionPanel widget;

    public SymbolComponentAdapter(SymbolSelectionPanel widget) {
        super(widget);
        this.widget = widget;
        this.widget.visible = true;

        // fix click in the background
        this.mouseDown().subscribe((input, doubled) -> true);
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        // fix scroll with smooth as it depends on the render
        // It is not being called because the custom implementation
        // of NonScrollableContainerWidget in Symbol Chat is not compatible with owo-lib by default
        this.widget.renderWidget(graphics, mouseX, mouseY, delta);
    }

}
