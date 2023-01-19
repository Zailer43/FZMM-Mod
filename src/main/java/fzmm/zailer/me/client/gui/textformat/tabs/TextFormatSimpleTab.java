package fzmm.zailer.me.client.gui.textformat.tabs;

import fzmm.zailer.me.client.gui.components.row.ColorRow;
import fzmm.zailer.me.client.gui.textformat.ITextFormatTab;
import fzmm.zailer.me.client.gui.textformat.TextFormatScreen;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

import java.util.function.Consumer;


@SuppressWarnings("UnstableApiUsage")
public class TextFormatSimpleTab implements ITextFormatTab {
    private static final String COLOR_ID = "color";

    private ConfigTextBox color;
    private Consumer<Object> callback;

    @Override
    public String getId() {
        return "simple";
    }

    @Override
    public Text getText(TextFormatLogic logic) {
        if (!this.color.isValid())
            return TextFormatScreen.EMPTY_COLOR_TEXT;
        Color color = (Color) this.color.parsedValue();

        return logic.getWithColor(color.rgb());
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.color = ColorRow.setup(rootComponent, COLOR_ID, Color.ofRgb(Integer.parseInt("FFFFFF", 16)), false, s -> this.callback.accept(""));
    }

    @Override
    public void setRandomValues() {
        Random random = Random.create();
        int initialColor = random.nextInt(0xFFFFFF);

        String hexColor = Integer.toHexString(initialColor);

        this.color.setText(hexColor);
    }

    @Override
    public void componentsCallback(Consumer<Object> callback) {
        this.callback = callback;
    }
}
