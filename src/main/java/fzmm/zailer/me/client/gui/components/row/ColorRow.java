package fzmm.zailer.me.client.gui.components.row;

import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.function.Consumer;

public class ColorRow extends AbstractRow {
    public ColorRow(String baseTranslationKey, String id, String tooltipId) {
        super(baseTranslationKey, id, tooltipId, true);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public Component[] getComponents(String id, String tooltipId) {
        Component colorField = new ConfigTextBox()
                .horizontalSizing(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(getColorFieldId(id));

        return new Component[] {
                colorField
        };
    }

    public static String getColorFieldId(String id) {
        return id + "-color";
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ConfigTextBox setup(FlowLayout rootComponent, String id, String defaultValue, @Nullable Consumer<String> changedListener) {
        ConfigTextBox colorField = ConfigTextBoxRow.setup(rootComponent, getColorFieldId(id), id, defaultValue, changedListener);
        colorField.inputPredicate(input -> input.matches("[0-9a-fA-F]{0,6}"));
        colorField.applyPredicate(s -> !s.isBlank());
        return colorField;
    }

    public static ColorRow parse(Element element) {
        String baseTranslationKey = getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new ColorRow(baseTranslationKey, id, tooltipId);
    }
}
