package fzmm.zailer.me.client.gui.text_format.tabs;

import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.ColorRow;
import fzmm.zailer.me.client.gui.text_format.TextFormatScreen;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import fzmm.zailer.me.client.logic.history.IMemento;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class TextFormatSimpleTab implements ITextFormatTab, IMemento {
    private ConfigTextBox color;

    @Override
    public String getId() {
        return "simple";
    }

    @Override
    public Text getText(TextFormatLogic logic) {
        if (!this.color.isValid()) return TextFormatScreen.EMPTY_COLOR_TEXT;
        Color color = (Color) this.color.parsedValue();

        return logic.getWithColor(color.rgb());
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.color = ColorRow.setup(rootComponent, "color", Color.WHITE, false, 0, s -> {
        });
    }

    @Override
    public void setRandomValues() {
        Color color = Color.ofRgb(Random.create().nextInt(0xFFFFFF));
        this.color.text(color.asHexString(false));
    }

    @Override
    public void componentsCallback(Consumer<Object> callback) {
        this.color.onChanged().subscribe(callback::accept);
    }

    @Override
    public boolean hasStyles() {
        return true;
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.color.getText());
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.color.text((String) input.readObject());
    }
}
