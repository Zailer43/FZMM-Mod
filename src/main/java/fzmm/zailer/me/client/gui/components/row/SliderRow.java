package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.core.Sizing;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.function.Consumer;

public class SliderRow extends AbstractRow {
    public SliderRow(String baseTranslationKey, String id, String tooltipId, boolean translation) {
        super(baseTranslationKey, id, tooltipId, true, translation);
    }

    @Override
    public UIComponent[] getComponents(String id, String tooltipId) {
        UIComponent slider = new SliderWidget()
                .horizontalSizing(Sizing.fixed(NORMAL_WIDTH))
                .id(getSliderId(id));

        return new UIComponent[] {
                slider
        };
    }

    public static String getSliderId(String id) {
        return id + "-slider";
    }

    @SuppressWarnings("UnstableApiUsage")
    public static SliderWidget setup(EFlowLayout rootComponent, String id, double defaultValue, double min,
                                     double max, Class<? extends Number> numberType, int decimalPlaces, double scrollStep, @Nullable Consumer<Double> callback) {
        SliderWidget numberSlider = rootComponent.childByIdOrThrow(SliderWidget.class, getSliderId(id));
        ButtonComponent resetButton = rootComponent.childById(ButtonComponent.class, getResetButtonId(id));

        numberSlider.decimalPlaces(decimalPlaces);
        numberSlider.valueType(numberType);
        numberSlider.onChanged().subscribe(aDouble -> {
            double discreteValue = numberSlider.discreteValue();
            if (resetButton != null) {
                resetButton.active = discreteValue != defaultValue;
            }
            if (callback != null) {
                callback.accept(discreteValue);
            }
        });
        numberSlider.min(min);
        numberSlider.max(max);
        numberSlider.setFromDiscreteValue(defaultValue);
        numberSlider.updateMessage();
        numberSlider.scrollStep(scrollStep / (max - min));

        if (resetButton != null) {
            resetButton.onPress(button -> numberSlider.setFromDiscreteValue(defaultValue));
            resetButton.active = false;
        }
        return numberSlider;
    }

    public SliderWidget getWidget() {
        return this.childById(SliderWidget.class, getSliderId(this.getId()));
    }

    public static SliderRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new SliderRow(baseTranslationKey, id, tooltipId, true);
    }
}
