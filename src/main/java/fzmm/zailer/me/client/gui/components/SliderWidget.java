package fzmm.zailer.me.client.gui.components;

import io.wispforest.owo.config.ui.component.ConfigSlider;
import io.wispforest.owo.ui.component.DiscreteSliderComponent;
import io.wispforest.owo.ui.component.SliderComponent;
import io.wispforest.owo.util.NumberReflection;
import net.minecraft.text.Text;

@SuppressWarnings("UnstableApiUsage")
public class SliderWidget extends ConfigSlider  {
    private boolean inverted = false;

    public void setDiscreteValueWithoutCallback(double discreteValue) {
        if (this.inverted)
            discreteValue = this.max - discreteValue;

        this.value = (discreteValue - this.min) / (this.max - this.min);
        this.updateMessage();
    }

    @Override
    public DiscreteSliderComponent setFromDiscreteValue(double discreteValue) {
        if (this.inverted)
            discreteValue = this.max - discreteValue;

        return super.setFromDiscreteValue(discreteValue);
    }

    @Override
    public SliderComponent value(double value) {
        if (this.inverted)
            value = 1 - value;
        return super.value(value);
    }

    @Override
    public void updateMessage() {
        super.updateMessage();
    }

    public void invertSlider() {
        this.inverted = true;

        this.message(s -> {
            double value = this.discreteValue();
            value = this.max - value;
            return Text.literal(String.valueOf(NumberReflection.convert(value, this.valueType)));
        });
    }
}
