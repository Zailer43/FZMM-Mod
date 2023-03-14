package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTabIdentifier;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class ScreenTabRow extends AbstractRow {
    public ScreenTabRow(String baseTranslationKey, String id) {
        super(baseTranslationKey);
        this.id(id);
        this.sizing(Sizing.content(), Sizing.fixed(28));
        this.surface(Surface.VANILLA_TRANSLUCENT);
        this.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.margins(Insets.bottom(4));
        this.gap(4);
    }

    @Override
    public Component[] getComponents(String id, String tooltipId) {
        return new Component[0];
    }

    public static String getScreenTabButtonId(String id) {
        return id + "-screen-tab-button";
    }

    public static String getScreenTabButtonId(IScreenTabIdentifier tab) {
        return getScreenTabButtonId(tab.getId());
    }

    public static void setup(FlowLayout rootComponent, String id, Enum<? extends IScreenTabIdentifier> defaultTab) {
        ScreenTabRow screenTabRow = rootComponent.childById(ScreenTabRow.class, id);
        if (screenTabRow == null)
            return;

        screenTabRow.setup(defaultTab);
    }

    public void setup(Enum<? extends IScreenTabIdentifier> defaultTab) {
        List<Component> componentList = new ArrayList<>();
        for (var tab : defaultTab.getClass().getEnumConstants()) {
            IScreenTabIdentifier screenTab = (IScreenTabIdentifier) tab;
            boolean active = tab != defaultTab;
            String translationKey = BaseFzmmScreen.getTabTranslationKey(this.baseTranslationKey) + screenTab.getId();
            Text text = Text.translatable(translationKey);
            ButtonWidget button = Components.button(text, buttonComponent -> {});

            button.id(getScreenTabButtonId(screenTab.getId()));
            button.active = active;

            componentList.add(button);
        }

        Component flowLayoutComponent = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .children(componentList)
                .gap(this.gap())
                .margins(Insets.vertical(4));
        Component scrollComponent = Containers.horizontalScroll(Sizing.fill(100), Sizing.fill(100), flowLayoutComponent)
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .margins(Insets.horizontal(20));

        this.child(scrollComponent);

    }

    public static ScreenTabRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);

        return new ScreenTabRow(baseTranslationKey, id);
    }
}
