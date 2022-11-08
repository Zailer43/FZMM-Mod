package fzmm.zailer.me.client.gui.textformat.tabs;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.textformat.ITextFormatTab;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

import java.awt.*;
import java.util.function.Consumer;


@SuppressWarnings("UnstableApiUsage")
public class TextFormatTwoColorsTab implements ITextFormatTab {
    private static final String INITIAL_COLOR_ID = "initialColor";
    private static final String FINAL_COLOR_ID = "finalColor";

    private ConfigTextBox initialColor;
    private ConfigTextBox finalColor;
    private Consumer<Object> callback;

    @Override
    public String getId() {
        return "two_colors";
    }

    @Override
    public Text getText(TextFormatLogic logic) {
        Color initialColor = new Color(Integer.parseInt(this.initialColor.getText(), 16), false);
        Color finalColor = new Color(Integer.parseInt(this.finalColor.getText(), 16), false);

        return logic.getGradient(initialColor, finalColor);
    }

    @Override
    public Component[] getComponents(BaseFzmmScreen parent) {
        return new Component[] {
                parent.newColorRow(INITIAL_COLOR_ID),
                parent.newColorRow(FINAL_COLOR_ID)
        };
    }

    @Override
    public void setupComponents(BaseFzmmScreen parent, FlowLayout rootComponent) {
        this.initialColor = parent.setupColorField(rootComponent, INITIAL_COLOR_ID, "FF0000", s -> this.callback.accept(""));
        this.finalColor = parent.setupColorField(rootComponent, FINAL_COLOR_ID, "0000FF", s -> this.callback.accept(""));
    }

    @Override
    public void setRandomValues() {
        Random random = Random.create();
        int initialColor = random.nextInt(0xFFFFFF);
        int finalColor = random.nextInt(0xFFFFFF);

        String hexInitialColor = Integer.toHexString(initialColor);
        String hexFinalColor = Integer.toHexString(finalColor);

        this.initialColor.setText(hexInitialColor);
        this.finalColor.setText(hexFinalColor);
    }

    @Override
    public void componentsCallback(Consumer<Object> callback) {
        this.callback = callback;
    }
}
