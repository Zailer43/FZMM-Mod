package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.function.Consumer;

public class SliderRow extends AbstractRow {
    public SliderRow(String baseTranslationKey, String id, String tooltipId) {
        super(baseTranslationKey, id, tooltipId, true);
    }

    @Override
    public Component[] getComponents(String id, String tooltipId) {
        Component slider = new SliderWidget()
                .horizontalSizing(Sizing.fixed(NORMAL_WIDTH))
                .id(getSliderId(id));

        return new Component[] {
                slider
        };
    }

    public static String getSliderId(String id) {
        return id + "-slider";
    }

    @SuppressWarnings("UnstableApiUsage")
    public static SliderWidget setup(FlowLayout rootComponent, String id, double defaultValue, double min,
                                     double max, Class<? extends Number> numberType, int decimalPlaces, @Nullable Consumer<Double> callback) {
        SliderWidget numberSlider = rootComponent.childById(SliderWidget.class, getSliderId(id));
        ButtonComponent resetButton = rootComponent.childById(ButtonComponent.class, getResetButtonId(id));

        BaseFzmmScreen.checkNull(numberSlider, "number-slider", getSliderId(id));
        BaseFzmmScreen.checkNull(resetButton, "button", getResetButtonId(id));

        numberSlider.decimalPlaces(decimalPlaces);
        numberSlider.valueType(numberType);
        numberSlider.onChanged().subscribe(aDouble -> {
            double discreteValue = numberSlider.discreteValue();
            resetButton.active = discreteValue != defaultValue;
            if (callback != null)
                callback.accept(discreteValue);
        });
        numberSlider.min(min);
        numberSlider.max(max);
        numberSlider.setFromDiscreteValue(defaultValue);

        resetButton.onPress(button -> numberSlider.setFromDiscreteValue(defaultValue));
        return numberSlider;
    }

    public static SliderRow parse(Element element) {
        String baseTranslationKey = getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new SliderRow(baseTranslationKey, id, tooltipId);
    }
}
