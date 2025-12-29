package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;

public class SnackBarLayout extends EFlowLayout implements ISnackBarScreen {
    public SnackBarLayout(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);
        this.positioning(Positioning.relative(100, 0));
        this.margins(Insets.right(3).withTop(3));
        this.horizontalAlignment(HorizontalAlignment.RIGHT);
    }

    @Override
    public FlowLayout getSnackBarLayout() {
        return this;
    }

    @Override
    public ParentComponent root() {
        return super.root();
    }
}
