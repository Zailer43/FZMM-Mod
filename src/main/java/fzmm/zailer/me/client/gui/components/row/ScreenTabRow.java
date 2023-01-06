package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.IScreenTab;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

public class ScreenTabRow extends AbstractRow {
    public ScreenTabRow(String baseTranslationKey, String id) {
        super(baseTranslationKey);
        this.id(id);
        this.sizing(Sizing.fill(100), Sizing.fixed(28));
        this.surface(Surface.VANILLA_TRANSLUCENT);
        this.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.margins(Insets.bottom(4));
    }

    @Override
    public Component[] getComponents(String id, String tooltipId) {
        return new Component[0];
    }

    public static String getScreenTabButtonId(String id) {
        return id + "-screen-tab-button";
    }

    public static String getScreenTabButtonId(IScreenTab tab) {
        return getScreenTabButtonId(tab.getId());
    }

    public static void setup(FlowLayout rootComponent, String id, Enum<? extends IScreenTab> defaultTab) {
        ScreenTabRow screenTabRow = rootComponent.childById(ScreenTabRow.class, id);
        if (screenTabRow == null)
            return;

        screenTabRow.setup(defaultTab);
    }

    public void setup(Enum<? extends IScreenTab> defaultTab) {
        for (var tab : defaultTab.getClass().getEnumConstants()) {
            IScreenTab screenTab = (IScreenTab) tab;
            boolean active = tab != defaultTab;
            String translationKey = BaseFzmmScreen.getTabTranslationKey(this.baseTranslationKey) + screenTab.getId();
            Text text = Text.translatable(translationKey);
            ButtonWidget button = Components.button(text, buttonComponent -> {});

            button.id(getScreenTabButtonId(screenTab.getId()))
                    .margins(Insets.horizontal(2));
            button.active = active;

            this.child(button);
        }
    }

    public static ScreenTabRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);

        return new ScreenTabRow(baseTranslationKey, id);
    }
}
