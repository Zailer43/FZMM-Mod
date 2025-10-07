package fzmm.zailer.me.client.gui.text_format.tabs;

import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.text_format.TextFormatScreen;
import fzmm.zailer.me.client.gui.text_format.components.ColorListContainer;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import fzmm.zailer.me.client.logic.history.IMemento;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.function.Consumer;

public class TextFormatGradientTab implements ITextFormatTab, IMemento {
    private ColorListContainer colorListContainer;

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
    public void setupComponents(EFlowLayout rootComponent) {
        this.colorListContainer = rootComponent.childByIdOrThrow(ColorListContainer.class, "gradientColorList");
    }

    @Override
    public void setRandomValues() {
        this.colorListContainer.setRandomColors();
    }

    @Override
    public void componentsCallback(Consumer<Object> callback) {
        this.colorListContainer.setCallback(callback::accept);
    }

    @Override
    public boolean hasStyles() {
        return true;
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException { // Color is not serializable
        output.writeObject(this.colorListContainer.getColors().stream().map(Color::rgb).toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.colorListContainer.setColors(((List<Integer>) input.readObject()).stream().map(Color::ofRgb).toList());
    }
}
