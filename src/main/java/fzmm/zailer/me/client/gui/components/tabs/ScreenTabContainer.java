package fzmm.zailer.me.client.gui.components.tabs;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class ScreenTabContainer extends FlowLayout {
    protected boolean selected;
    protected List<Component> componentList;

    public ScreenTabContainer(String baseTranslationKey, Sizing horizontalSizing, Sizing verticalSizing, String id) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);
        this.selected = false;
        this.componentList = new ArrayList<>();
        this.id(getScreenTabId(id));

        String translationKey = "fzmm.gui." + baseTranslationKey + ".tab." + id;

        this.child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                        .child(
                                Components.label(Text.translatable(translationKey))
                                        .tooltip(Text.translatable(translationKey + ".tooltip"))
                        ).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                        .margins(Insets.vertical(4))
        );
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

    @Override
    protected void updateLayout() {
        super.updateLayout();
    }

    public static ScreenTabContainer parse(Element element) {
        String id = UIParsing.parseText(UIParsing.childElements(element).get("id")).getString();
        return new ScreenTabContainer(BaseFzmmScreen.getBaseTranslationKey(element), Sizing.content(), Sizing.content(), id);
    }
}
