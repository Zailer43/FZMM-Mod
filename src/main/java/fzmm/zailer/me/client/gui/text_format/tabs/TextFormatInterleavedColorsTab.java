package fzmm.zailer.me.client.gui.text_format.tabs;

import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.text_format.TextFormatScreen;
import fzmm.zailer.me.client.gui.text_format.components.ColorListContainer;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class TextFormatInterleavedColorsTab implements ITextFormatTab {
    private ColorListContainer colorListContainer;
    private SliderWidget distanceField;
    private Consumer<Object> callback;

    @Override
    public String getId() {
        return "interleaved_colors";
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Text getText(TextFormatLogic logic) {
        if (!this.colorListContainer.isValid())
            return TextFormatScreen.EMPTY_COLOR_TEXT;
        List<Color> colorList = this.colorListContainer.getColors();
        int distance = (int) this.distanceField.parsedValue();
        return logic.getInterleaved(colorList, distance);
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.distanceField = SliderRow.setup(rootComponent, "interleavedDistance", 1, 1, 25, Integer.class, 0, 1, this.callback::accept);
        this.colorListContainer = rootComponent.childByIdOrThrow(ColorListContainer.class, "interleavedColorList");
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

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public IMementoObject createMemento() {
        return new InterleavedColorsMementoTab(this.colorListContainer.getColors(), (int) this.distanceField.parsedValue());
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        InterleavedColorsMementoTab memento = (InterleavedColorsMementoTab) mementoTab;
        this.colorListContainer.setColors(memento.colors);
        this.distanceField.setFromDiscreteValue(memento.distance);
    }

    private record InterleavedColorsMementoTab(List<Color> colors, int distance) implements IMementoObject {
    }
}
