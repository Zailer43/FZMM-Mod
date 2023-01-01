package fzmm.zailer.me.compat.symbolChat.symbol;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.AnimatableProperty;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.util.math.MatrixStack;

public class SymbolSelectionPanelComponent extends BaseComponent{

    private final AbstractParentElement selectionPanel;

    public SymbolSelectionPanelComponent(AbstractParentElement symbolSelectionPanel) {
        super();
        this.selectionPanel = symbolSelectionPanel;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        ((net.replaceitem.symbolchat.gui.SymbolSelectionPanel) this.selectionPanel).render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (((net.replaceitem.symbolchat.gui.SymbolSelectionPanel) this.selectionPanel).visible && this.selectionPanel.mouseClicked(mouseX, mouseY, button))
            return true;
        return super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        if (((net.replaceitem.symbolchat.gui.SymbolSelectionPanel) this.selectionPanel).visible && this.selectionPanel.mouseScrolled(mouseX, mouseY, amount))
            return true;
        return super.onMouseScroll(mouseX, mouseY, amount);
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
}
