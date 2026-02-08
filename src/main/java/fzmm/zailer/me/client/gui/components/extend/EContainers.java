package fzmm.zailer.me.client.gui.components.extend;

import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.extend.container.EScrollContainer;
import fzmm.zailer.me.client.gui.components.tabs.TabContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;

/**
 * Extended containers of the owo-lib containers
 */
public class EContainers {
    public static EFlowLayout verticalFlow(Sizing horizontalSizing, Sizing verticalSizing) {
        return new EFlowLayout(horizontalSizing, verticalSizing, FlowLayout.Algorithm.VERTICAL);
    }

    public static EFlowLayout horizontalFlow(Sizing horizontalSizing, Sizing verticalSizing) {
        return new EFlowLayout(horizontalSizing, verticalSizing, FlowLayout.Algorithm.HORIZONTAL);
    }

    public static EFlowLayout ltrTextFlow(Sizing horizontalSizing, Sizing verticalSizing) {
        return new EFlowLayout(horizontalSizing, verticalSizing, FlowLayout.Algorithm.LTR_TEXT);
    }

    public static <C extends UIComponent> EScrollContainer<C> verticalScroll(Sizing horizontalSizing, Sizing verticalSizing, C child) {
        return verticalScroll(horizontalSizing, verticalSizing, child, false);
    }

    public static <C extends UIComponent> EScrollContainer<C> verticalScroll(Sizing horizontalSizing, Sizing verticalSizing, C child, boolean flipScroll) {
        return new EScrollContainer<>(ScrollContainer.ScrollDirection.VERTICAL, horizontalSizing, verticalSizing, child, flipScroll);
    }

    public static <C extends UIComponent> EScrollContainer<C> horizontalScroll(Sizing horizontalSizing, Sizing verticalSizing, C child) {
        return horizontalScroll(horizontalSizing, verticalSizing, child, false);
    }

    public static <C extends UIComponent> EScrollContainer<C> horizontalScroll(Sizing horizontalSizing, Sizing verticalSizing, C child, boolean flipScroll) {
        return new EScrollContainer<>(ScrollContainer.ScrollDirection.HORIZONTAL, horizontalSizing, verticalSizing, child, flipScroll);
    }

    public static TabContainer tabHorizontal(Sizing horizontalSizing, Sizing verticalSizing) {
        return new TabContainer(horizontalSizing, verticalSizing, FlowLayout.Algorithm.HORIZONTAL);
    }

    public static TabContainer tabVertical(Sizing horizontalSizing, Sizing verticalSizing) {
        return new TabContainer(horizontalSizing, verticalSizing, FlowLayout.Algorithm.VERTICAL);
    }

    public static TabContainer tabLtrTextFlow(Sizing horizontalSizing, Sizing verticalSizing) {
        return new TabContainer(horizontalSizing, verticalSizing, FlowLayout.Algorithm.LTR_TEXT);
    }
}
