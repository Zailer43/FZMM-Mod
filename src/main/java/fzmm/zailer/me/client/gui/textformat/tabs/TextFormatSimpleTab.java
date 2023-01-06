package fzmm.zailer.me.client.gui.textformat.tabs;

import fzmm.zailer.me.client.gui.components.row.ColorRow;
import fzmm.zailer.me.client.gui.textformat.ITextFormatTab;
import fzmm.zailer.me.client.gui.textformat.TextFormatScreen;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.container.FlowLayout;
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
        String text = this.color.getText().replaceAll("#", "");
        if (text.isBlank())
            return TextFormatScreen.EMPTY_COLOR_TEXT;

        int color = Integer.parseInt(text, 16);

        return logic.getWithColor(color);
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.color = ColorRow.setup(rootComponent, COLOR_ID, "FFFFFF", s -> this.callback.accept(""));
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
