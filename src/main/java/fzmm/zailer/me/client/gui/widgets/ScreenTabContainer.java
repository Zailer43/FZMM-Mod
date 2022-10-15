package fzmm.zailer.me.client.gui.widgets;

import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class ScreenTabContainer extends VerticalFlowLayout {
    protected boolean selected;
    protected List<Component> componentList;

    public ScreenTabContainer(Sizing horizontalSizing, Sizing verticalSizing, boolean selected) {
        super(horizontalSizing, verticalSizing);
        this.selected = selected;
        this.componentList = new ArrayList<>();
    }

    public void setSelected(boolean selected) {
        if (this.selected && selected)
            return;
        this.selected = selected;

        if (this.selected) {
            this.children.addAll(this.componentList);
            this.componentList.clear();
        } else {
            this.componentList.addAll(this.children);
            this.children.clear();
        }

        this.updateLayout();
    }

    @Override
    protected void updateLayout() {
        super.updateLayout();
    }

    public static ScreenTabContainer parse(Element element) {
        boolean selected = element.getAttribute("selected").equals("true");
        return new ScreenTabContainer(Sizing.content(), Sizing.content(), selected);
    }
}
