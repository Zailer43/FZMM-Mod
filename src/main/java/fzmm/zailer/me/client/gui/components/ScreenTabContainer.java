package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.client.gui.IScreenTab;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class ScreenTabContainer extends VerticalFlowLayout {
    protected boolean selected;
    protected List<Component> componentList;

    public ScreenTabContainer(Sizing horizontalSizing, Sizing verticalSizing, String id) {
        super(horizontalSizing, verticalSizing);
        this.selected = false;
        this.componentList = new ArrayList<>();
        this.id(getScreenTabId(id));
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

    public static String getScreenTabId(String id) {
        return id + "-screen-tab";
    }

    public static String getScreenTabId(IScreenTab tab) {
        return getScreenTabId(tab.getId());
    }

    @Override
    protected void updateLayout() {
        super.updateLayout();
    }

    public static ScreenTabContainer parse(Element element) {
        String id = UIParsing.parseText(UIParsing.childElements(element).get("id")).getString();
        return new ScreenTabContainer(Sizing.content(), Sizing.content(), id);
    }
}
