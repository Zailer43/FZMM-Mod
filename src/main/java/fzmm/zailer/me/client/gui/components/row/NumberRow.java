package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.NumberReflection;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.function.Consumer;

public class NumberRow extends AbstractRow {
    public NumberRow(String baseTranslationKey, String id, String tooltipId) {
        super(baseTranslationKey, id, tooltipId, true);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public Component[] getComponents(String id, String tooltipId) {
        Component numberField = new ConfigTextBox()
                .horizontalSizing(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(getNumberFieldId(id));

        return new Component[] {
                numberField
        };
    }

    public static String getNumberFieldId(String id) {
        return id + "-number-field";
    }


    @SuppressWarnings("UnstableApiUsage")
    public static ConfigTextBox setup(FlowLayout rootComponent, String id, double defaultValue, Class<? extends Number> numberType) {
        return setup(rootComponent, id, defaultValue, numberType, null);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ConfigTextBox setup(FlowLayout rootComponent, String id, double defaultValue, Class<? extends Number> numberType, @Nullable Consumer<String> changedListener) {
        String defaultValueString = NumberReflection.isFloatingPointType(numberType) ? String.valueOf(defaultValue) : String.valueOf((int) defaultValue);
        ConfigTextBox numberBox = ConfigTextBoxRow.setup(rootComponent, getNumberFieldId(id), id, defaultValueString, changedListener, s -> {
            try {
                return !s.isBlank() && defaultValue == Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return false;
            }
        });
        numberBox.configureForNumber(numberType);
        return numberBox;
    }

    public static NumberRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new NumberRow(baseTranslationKey, id, tooltipId);
    }
}
