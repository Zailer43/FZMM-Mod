package fzmm.zailer.me.client.gui.textformat.tabs;

import fzmm.zailer.me.client.gui.textformat.ITextFormatTab;
import fzmm.zailer.me.client.gui.textformat.TextFormatScreen;
import fzmm.zailer.me.client.gui.textformat.components.ColorListContainer;
import fzmm.zailer.me.client.gui.utils.IMementoObject;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
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
        List<Color> colorList = this.colorListContainer.getColors();
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

    @Override
    public boolean hasStyles() {
        return true;
    }


    @Override
    public IMementoObject createMemento() {
        return new GradientMementoTab(this.colorListContainer.getColors());
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        GradientMementoTab memento = (GradientMementoTab) mementoTab;
        this.colorListContainer.setColors(memento.colors);
    }

    private record GradientMementoTab(List<Color> colors) implements IMementoObject {
    }
}
