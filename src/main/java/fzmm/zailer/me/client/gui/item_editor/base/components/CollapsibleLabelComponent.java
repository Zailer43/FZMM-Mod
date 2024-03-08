package fzmm.zailer.me.client.gui.item_editor.base.components;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class CollapsibleLabelComponent extends LabelComponent implements ICollapsible {
    private final Text collapsedText;
    private Text expandedText;
    private boolean isCollapsed = false;

    public CollapsibleLabelComponent(Text expandedText, Text collapsedText) {
        super(Text.empty());
        this.collapsedText = collapsedText;
        this.expandedText = expandedText;
        this.text(expandedText);
        this.horizontalSizing(Sizing.expand(100));
    }

    @Override
    public void collapse() {
        this.isCollapsed = true;
        this.text = this.collapsedText;
        this.tooltip(this.expandedText);
    }

    @Override
    public void expand() {
        this.isCollapsed = false;
        this.text = this.expandedText;
        this.tooltip(new ArrayList<TooltipComponent>());
    }

    @Override
    public LabelComponent text(Text text) {
        this.expandedText = text;

        return super.text(this.isCollapsed ? this.collapsedText : this.expandedText);
    }
}
