package fzmm.zailer.me.compat.symbolChat.symbol;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.compat.CompatMods;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.DrawContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SymbolSelectionPanelComponentAdapter extends BaseComponent {

    private final AbstractParentElement selectionPanel;
    private final Field isVisibleField;
    private Method selectionPanelRenderMethod;
    private int contentWidth;
    private int contentHeight;

    public SymbolSelectionPanelComponentAdapter(AbstractParentElement symbolSelectionPanel, Field visibleField, Class<?> symbolSelectionPanelClass) {
        super();
        this.selectionPanel = symbolSelectionPanel;
        this.isVisibleField = visibleField;

        this.mouseDown().subscribe((mouseX, mouseY, button) ->
                this.isVisible() && this.selectionPanel.mouseClicked(mouseX, mouseY, button));

        this.mouseScroll().subscribe((mouseX, mouseY, amount) ->
                this.isVisible() && this.selectionPanel.mouseScrolled(mouseX, mouseY, amount));

        this.keyPress().subscribe(this.selectionPanel::keyPressed);
        this.charTyped().subscribe(this.selectionPanel::charTyped);

        try {
            this.selectionPanelRenderMethod = symbolSelectionPanelClass.getMethod("render", DrawContext.class, int.class, int.class, float.class);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolSelectionPanelComponentAdapter] Failed to find render method", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
        }

        try {
            this.contentWidth = symbolSelectionPanelClass.getField("WIDTH").getInt(null);
            this.contentHeight = symbolSelectionPanelClass.getField("HEIGHT").getInt(null);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolSelectionPanelComponentAdapter] Failed to find component size", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
        }
    }

    public boolean isVisible() {
        try {
            return this.isVisibleField.getBoolean(this.selectionPanel);
        } catch (Exception e) {
            CompatMods.SYMBOL_CHAT_PRESENT = false;
            return false;
        }
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT)
            return;

        try {
            this.selectionPanelRenderMethod.invoke(this.selectionPanel, context, mouseX, mouseY, delta);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SymbolSelectionPanelComponentAdapter] Failed to invoke render method", e);
            CompatMods.SYMBOL_CHAT_PRESENT = false;
        }
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return this.contentWidth;
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return this.contentHeight;
    }

    @Override
    public boolean isInBoundingBox(double x, double y) {
        if (!this.isVisible())
            return false;
        return super.isInBoundingBox(x, y);
    }
}
