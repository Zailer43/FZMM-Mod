package fzmm.zailer.me.client.gui.textformat.tabs;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.textformat.ITextFormatTab;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import fzmm.zailer.me.config.FzmmConfig;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.text.Text;

import java.util.Date;
import java.util.Random;
import java.util.function.Consumer;

public class TextFormatRainbowTab implements ITextFormatTab {
    private static final String HUE_ID = "hue";
    private static final String BRIGHTNESS_ID = "brightness";
    private static final String SATURATION_ID = "saturation";
    private static final String HUE_STEP_ID = "hueStep";

    private SliderWidget hue;
    private SliderWidget brightness;
    private SliderWidget saturation;
    private SliderWidget hueStep;
    private Consumer<Object> callback;

    @Override
    public String getId() {
        return "rainbow";
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Text getText(TextFormatLogic logic) {
        float hue = (float) this.hue.parsedValue();
        float saturation = (float) this.saturation.parsedValue();
        float brightness = (float) this.brightness.parsedValue();
        float hueStep = (float) this.hueStep.parsedValue();

        return logic.getRainbow(hue, saturation, brightness, hueStep);
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        FzmmConfig.TextFormat config = FzmmClient.CONFIG.textFormat;
        this.hue = SliderRow.setup(rootComponent, HUE_ID, 1d, 0d, 1d, Float.class, 4, d -> this.callback.accept(""));
        this.brightness = SliderRow.setup(rootComponent, BRIGHTNESS_ID, 0.8d, 0d, 1d, Float.class, 4, d -> this.callback.accept(""));
        this.saturation = SliderRow.setup(rootComponent, SATURATION_ID, 1d, 0d, 1d, Float.class, 4, d -> this.callback.accept(""));
        this.hueStep = SliderRow.setup(rootComponent, HUE_STEP_ID, 0.05d, config.minRainbowHueStep(), config.maxRainbowHueStep(), Float.class, 4, d -> this.callback.accept(""));
    }

    @Override
    public void setRandomValues() {
        FzmmConfig.TextFormat config = FzmmClient.CONFIG.textFormat;

        Random random = new Random(new Date().getTime());
        float hue = random.nextFloat();
        float saturation = random.nextFloat();
        float brightness = random.nextFloat();
        float hueStep = random.nextFloat(config.minRainbowHueStep(), config.maxRainbowHueStep());

        this.hue.setFromDiscreteValue(hue);
        this.saturation.setFromDiscreteValue(saturation);
        this.brightness.setFromDiscreteValue(brightness);
        this.hueStep.setFromDiscreteValue(hueStep);
    }

    @Override
    public void componentsCallback(Consumer<Object> callback) {
        this.callback = callback;
    }
}
