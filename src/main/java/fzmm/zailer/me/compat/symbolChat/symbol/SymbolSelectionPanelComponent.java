package fzmm.zailer.me.compat.symbolChat.symbol;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.AnimatableProperty;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.AbstractParentElement;

public class SymbolSelectionPanelComponent extends BaseComponent {

    private final AbstractParentElement selectionPanel;

    public SymbolSelectionPanelComponent(AbstractParentElement symbolSelectionPanel) {
        super();
        this.selectionPanel = symbolSelectionPanel;

        this.mouseDown().subscribe((mouseX, mouseY, button) ->
                ((net.replaceitem.symbolchat.gui.SymbolSelectionPanel) this.selectionPanel).visible && this.selectionPanel.mouseClicked(mouseX, mouseY, button));

        this.mouseScroll().subscribe((mouseX, mouseY, amount) ->
                        ((net.replaceitem.symbolchat.gui.SymbolSelectionPanel) this.selectionPanel).visible && this.selectionPanel.mouseScrolled(mouseX, mouseY, amount));
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        ((net.replaceitem.symbolchat.gui.SymbolSelectionPanel) this.selectionPanel).render(context, mouseX, mouseY, delta);
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return net.replaceitem.symbolchat.gui.SymbolSelectionPanel.WIDTH;
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return net.replaceitem.symbolchat.gui.SymbolSelectionPanel.HEIGHT;
    }

    @Override
    public AnimatableProperty<Positioning> positioning() {
        return AnimatableProperty.of(Positioning.relative(0, 0));
    }

    public AbstractParentElement getSymbolSelectionPanel() {
        return this.selectionPanel;
    }

    @Override
    public boolean isInBoundingBox(double x, double y) {
        if (!((net.replaceitem.symbolchat.gui.SymbolSelectionPanel) this.selectionPanel).visible)
            return false;
        return super.isInBoundingBox(x, y);
    }
}
