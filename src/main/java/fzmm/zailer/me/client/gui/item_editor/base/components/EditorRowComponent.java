package fzmm.zailer.me.client.gui.item_editor.base.components;

import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class EditorRowComponent extends FlowLayout implements ICollapsible {
    private static final int APPLICABLE_EDITORS_CATEGORY_BACKGROUND_COLOR = 0xff7d7d7d;
    private static final int SELECTED_CATEGORY_BACKGROUND_COLOR = 0xff5da25f;
    private static final int NON_APPLICABLE_EDITORS_CATEGORY_BACKGROUND_COLOR = 0xff585858;

    private final ItemComponent exampleItem;
    private final Text label;

    public EditorRowComponent(IItemEditorScreen itemEditorScreen, boolean isApplicable, boolean isSelected) {
        super(Sizing.fill(100), Sizing.content(), Algorithm.HORIZONTAL);
        this.gap(2);
        this.verticalAlignment(VerticalAlignment.CENTER);

        this.exampleItem = Components.item(itemEditorScreen.getExampleItem()).showOverlay(true);
        this.label = itemEditorScreen.getEditorLabel();

        int backgroundColor = NON_APPLICABLE_EDITORS_CATEGORY_BACKGROUND_COLOR;

        if (isSelected)
            backgroundColor = SELECTED_CATEGORY_BACKGROUND_COLOR;
        else if (isApplicable)
            backgroundColor = APPLICABLE_EDITORS_CATEGORY_BACKGROUND_COLOR;

        this.exampleItem.cursorStyle(CursorStyle.HAND);
        this.cursorStyle(CursorStyle.HAND);

        this.surface(Surface.flat(backgroundColor));
    }

    @Override
    public void collapse() {
        this.clearChildren();
        this.child(this.exampleItem.tooltip(this.label));
    }

    @Override
    public void expand() {
        this.clearChildren();
        this.child(this.exampleItem.tooltip(new ArrayList<TooltipComponent>()));
        this.child(Components.label(this.label).cursorStyle(CursorStyle.HAND));
    }
}
