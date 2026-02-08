package fzmm.zailer.me.client.gui.text_format.tabs;

import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.text_format.TextFormatScreen;
import fzmm.zailer.me.client.gui.text_format.components.ColorListContainer;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import fzmm.zailer.me.client.logic.history.IMemento;
import io.wispforest.owo.ui.core.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public class TextFormatInterleavedColorsTab implements ITextFormatTab, IMemento {
    private ColorListContainer colorListContainer;
    private SliderWidget distanceField;

    @Override
    public String getId() {
        return "interleaved_colors";
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Component getText(TextFormatLogic logic) {
        if (!this.colorListContainer.isValid())
            return TextFormatScreen.EMPTY_COLOR_TEXT;
        List<Color> colorList = this.colorListContainer.getColors();
        int distance = (int) this.distanceField.parsedValue();
        return logic.getInterleaved(colorList, distance);
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.distanceField = SliderRow.setup(rootComponent, "interleavedDistance", 1, 1, 25, Integer.class, 0, 1, aDouble -> {});
        this.colorListContainer = rootComponent.childByIdOrThrow(ColorListContainer.class, "interleavedColorList");
    }

    @Override
    public void setRandomValues() {
        this.colorListContainer.setRandomColors();
    }

    @Override
    public void componentsCallback(Consumer<Object> callback) {
        this.distanceField.onChanged().subscribe(callback::accept);
        this.colorListContainer.setCallback(callback::accept);
    }

    @Override
    public boolean hasStyles() {
        return true;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.colorListContainer.getColors().stream().map(Color::rgb).toList());
        output.writeInt((int) this.distanceField.parsedValue());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.colorListContainer.setColors(((List<Integer>) input.readObject()).stream().map(Color::ofRgb).toList());
        this.distanceField.setFromDiscreteValue(input.readInt());
    }
}
