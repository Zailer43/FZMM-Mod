package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.function.Consumer;

public class ColorRow extends AbstractRow {
    public ColorRow(String baseTranslationKey, String id, String tooltipId) {
        this(baseTranslationKey, id, tooltipId, true);
    }

    public ColorRow(String baseTranslationKey, String id, String tooltipId, boolean hasResetButton) {
        super(baseTranslationKey, id, tooltipId, hasResetButton);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public Component[] getComponents(String id, String tooltipId) {
        Component colorField = new ConfigTextBox()
                .horizontalSizing(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(getColorFieldId(id));

        return new Component[]{
                colorField
        };
    }

    public static String getColorFieldId(String id) {
        return id + "-color";
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ConfigTextBox setup(FlowLayout rootComponent, String id, String defaultValue, @Nullable Consumer<String> changedListener) {
        ConfigTextBox colorField = ConfigTextBoxRow.setup(rootComponent, getColorFieldId(id), id, defaultValue, changedListener);
        colorField.inputPredicate(input -> input.matches("#?[0-9a-fA-F]{0,6}"));
        colorField.applyPredicate(s -> !s.isBlank());
        return colorField;
    }

    public static ColorRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new ColorRow(baseTranslationKey, id, tooltipId);
    }

    @SuppressWarnings("UnstableApiUsage")
    public void setColor(String color) {
        ConfigTextBox colorField = this.childById(ConfigTextBox.class, getColorFieldId(this.getId()));
        if (colorField != null)
            colorField.setText(color);
    }

    @SuppressWarnings("UnstableApiUsage")
    public int getColor() {
        ConfigTextBox colorField = this.childById(ConfigTextBox.class, getColorFieldId(this.getId()));
        if (colorField == null || !colorField.isValid())
            return 0;

        String text = colorField.getText().replaceAll("#", "");
        if (text.isBlank())
            return 0;
        return Integer.valueOf(text, 16);
    }

    @SuppressWarnings("UnstableApiUsage")
    public String getText() {
        ConfigTextBox colorField = this.childById(ConfigTextBox.class, getColorFieldId(this.getId()));
        if (colorField == null)
            return "FFFFFF";
        return colorField.getText();
    }
}
