package fzmm.zailer.me.client.gui.components.tabs;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class ScreenTabContainer extends EFlowLayout {
    protected boolean selected;
    protected List<Component> componentList;
    private final FlowLayout labelLayout;

    public ScreenTabContainer(String baseTranslationKey, Sizing horizontalSizing, Sizing verticalSizing, String id) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);
        this.selected = false;
        this.componentList = null;
        this.id(getScreenTabId(id));

        String translationKey = "fzmm.gui." + baseTranslationKey + ".tab." + id;

        this.labelLayout = (FlowLayout) EContainers.horizontalFlow(Sizing.fill(100), Sizing.content())
                .child(
                        EComponents.label(Text.translatable(translationKey))
                                .tooltip(Text.translatable(translationKey + ".tooltip"))
                                .margins(Insets.vertical(4))
                ).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    public void setSelected(boolean select, boolean addLabel) {
        if (this.selected && select) {
            return;
        }
        this.selected = select;

        if (this.componentList == null) {
            this.componentList = new ArrayList<>(this.children());
        }
        this.clearChildren();

        if (select) {
            if (addLabel) {
                this.child(this.labelLayout);
            }
            this.children(this.componentList);
        }
    }

    public static String getScreenTabId(String id) {
        return id + "-screen-tab";
    }

    public static ScreenTabContainer parse(Element element) {
        String id = UIParsing.parseText(UIParsing.childElements(element).get("id")).getString();
        return new ScreenTabContainer(BaseFzmmScreen.getBaseTranslationKey(element), Sizing.content(), Sizing.content(), id);
    }
}
