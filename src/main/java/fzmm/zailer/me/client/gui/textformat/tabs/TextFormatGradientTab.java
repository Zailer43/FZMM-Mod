package fzmm.zailer.me.client.gui.textformat.tabs;

import fzmm.zailer.me.client.gui.textformat.ITextFormatTab;
import fzmm.zailer.me.client.gui.textformat.TextFormatScreen;
import fzmm.zailer.me.client.gui.textformat.components.ColorListContainer;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class TextFormatGradientTab implements ITextFormatTab {
    private static final String COLOR_LIST_ID = "gradientColorList";

    private ColorListContainer colorListContainer;
    private Consumer<Object> callback;

    @Override
    public String getId() {
        return "gradient";
    }

    @Override
    public Text getText(TextFormatLogic logic) {
        if (!this.colorListContainer.isValid())
            return TextFormatScreen.EMPTY_COLOR_TEXT;
        List<Integer> colorList = this.colorListContainer.getColors();
        return logic.getGradient(colorList);
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.colorListContainer = rootComponent.childById(ColorListContainer.class, COLOR_LIST_ID);
        assert this.colorListContainer != null;
        this.colorListContainer.setCallback(this.callback::accept);
    }

    @Override
    public void setRandomValues() {
        this.colorListContainer.setRandomColors();
    }

    @Override
    public void componentsCallback(Consumer<Object> callback) {
        this.callback = callback;
    }
}
