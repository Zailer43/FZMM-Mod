package fzmm.zailer.me.client.gui.textformat.components;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.row.AbstractRow;
import fzmm.zailer.me.client.gui.components.row.ColorRow;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ColorListContainer extends FlowLayout {
    public static final int MINIMUM_SIZE = 2;
    public static final Text ADD_COLOR_TEXT = Text.translatable("fzmm.gui.button.colorList.add");
    public static final String COLOR_AMOUNT_TRANSLATION_KEY = "fzmm.gui.button.colorList.amount";
    private final FlowLayout colorsLayout;
    private final LabelComponent colorAmountLabel;
    private final Random random;
    private Consumer<String> callback;

    public ColorListContainer(String id, String tooltipId, String baseTranslationKey) {
        super(Sizing.fill(100), Sizing.content(), Algorithm.VERTICAL);
        this.random = Random.create();
        this.callback = s -> {};
        this.id(id);
        this.gap(BaseFzmmScreen.COMPONENT_DISTANCE);

        FlowLayout topLayout = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(AbstractRow.TOTAL_HEIGHT));
        topLayout.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        Component labelComponent = AbstractRow.getLabel(id, tooltipId, BaseFzmmScreen.getOptionBaseTranslationKey(baseTranslationKey));
        this.colorsLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        this.colorAmountLabel = Components.label(Text.translatable(COLOR_AMOUNT_TRANSLATION_KEY, this.colorsLayout.children().size()));

        ButtonComponent addButton = Components.button(ADD_COLOR_TEXT, buttonComponent -> this.addEntry());

        topLayout.child(labelComponent);
        topLayout.child(addButton);
        topLayout.child(this.colorAmountLabel);
        topLayout.gap(BaseFzmmScreen.COMPONENT_DISTANCE);

        this.child(topLayout);
        this.child(this.colorsLayout);

        this.addEntry();
        this.addEntry();
    }

    public void addEntry() {
        int id = this.colorsLayout.children().size();
        ColorListEntry entry = new ColorListEntry(this, id);
        ColorRow.setup(entry, String.valueOf(id), this.getRandomColor(), false, this.callback);
        this.colorsLayout.child(entry);
        this.updateDisplay();
        this.callback.accept("");
    }

    @SuppressWarnings("UnstableApiUsage")
    public void setCallback(Consumer<String> callback) {
        this.callback = callback;

        for (int i = 0; i != this.colorsLayout.children().size(); i++) {
            ConfigTextBox colorField = this.childById(ConfigTextBox.class, ColorRow.getColorFieldId(String.valueOf(i)));
            if (colorField != null) {
                colorField.onChanged().subscribe(callback::accept);
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public boolean isValid() {
        for (int i = 0; i != this.colorsLayout.children().size(); i++) {
            ConfigTextBox colorField = this.childById(ConfigTextBox.class, ColorRow.getColorFieldId(String.valueOf(i)));
            if (colorField != null) {
                if (!colorField.isValid())
                    return false;
            }
        }
        return true;
    }

    public Color getRandomColor() {
        return  Color.ofRgb(this.random.nextInt(0xFFFFFF));
    }

    public void setRandomColors() {
        for (var component : this.colorsLayout.children()) {
            if (component instanceof ColorListEntry colorEntry) {
                colorEntry.setColor(this.getRandomColor());
            }
        }
    }

    public List<Color> getColors() {
        List<Color> colorList = new ArrayList<>();

        for (var component : this.colorsLayout.children()) {
            if (component instanceof ColorListEntry colorEntry) {
                colorList.add(colorEntry.getColor());
            }
        }

        return colorList;
    }

    public static ColorListContainer parse(Element element) {
        String id = AbstractRow.getId(element);
        String tooltipId = AbstractRow.getTooltipId(element, id);
        return new ColorListContainer(id, tooltipId, BaseFzmmScreen.getBaseTranslationKey(element));
    }

    public void removeColorEntry(ColorListEntry entry) {
        if (this.colorsLayout.children().size() > MINIMUM_SIZE)
            this.colorsLayout.removeChild(entry);
        this.updateDisplay();
        this.callback.accept("");
    }

    private void moveEntry(int entryIndex, int direction) {
        List<Component> colorLayoutChildren = new ArrayList<>(this.colorsLayout.children());

        if (direction == -1 && entryIndex > 0)
            Collections.swap(colorLayoutChildren, entryIndex, entryIndex - 1);
        else if (direction == 1 && entryIndex < colorLayoutChildren.size() - 1)
            Collections.swap(colorLayoutChildren, entryIndex, entryIndex + 1);

        // this resets the scroll, it's very annoying
//        this.colorsLayout.clearChildren();
//        this.colorsLayout.children(colorLayoutChildren);

        List<Color> colors = new ArrayList<>();
        for (var entry : colorLayoutChildren) {
            if (entry instanceof ColorListEntry colorEntry)
                colors.add(colorEntry.getColor());
        }

        for (int i = 0; i != colorLayoutChildren.size(); i++) {
            Component component = this.colorsLayout.children().get(i);
            if (component instanceof ColorListEntry colorEntry) {
                colorEntry.setColor(colors.get(i));
            }
        }

        this.updateMoveButtons();
        this.callback.accept("");
    }

    public void upEntry(ColorListEntry entry) {
        int entryIndex = this.colorsLayout.children().indexOf(entry);
        this.moveEntry(entryIndex, -1);
    }

    public void downEntry(ColorListEntry entry) {
        int entryIndex = this.colorsLayout.children().indexOf(entry);
        this.moveEntry(entryIndex, 1);
    }

    public void updateDisplay() {
        this.updateMoveButtons();
        this.updateRemoveButton();
        this.colorAmountLabel.text(Text.translatable(COLOR_AMOUNT_TRANSLATION_KEY, this.colorsLayout.children().size()));
    }

    public void updateRemoveButton() {
        boolean active = this.colorsLayout.children().size() > MINIMUM_SIZE;

        for (var component : this.colorsLayout.children()) {
            if (component instanceof ColorListEntry colorEntry) {
                colorEntry.setRemoveButtonActive(active);
            }
        }
    }

    public void updateMoveButtons() {
        int size = this.colorsLayout.children().size();

        for (int i = 0; i != size; i ++) {
            if (this.colorsLayout.children().get(i) instanceof ColorListEntry entry) {
                if (i == 0) {
                    entry.setMoveUpButtonActive(false);
                    entry.setMoveDownButtonActive(true);
                } else if (i == size - 1) {
                    entry.setMoveUpButtonActive(true);
                    entry.setMoveDownButtonActive(false);
                } else {
                    entry.setMoveUpButtonActive(true);
                    entry.setMoveDownButtonActive(true);
                }
            }
        }
    }

    public void setColors(List<Color> colors) {
        List<Component> colorLayoutChildren = this.colorsLayout.children();
        while (colors.size() > colorLayoutChildren.size()) {
            this.addEntry();
        }

        for (int i = 0; i != colors.size(); i++) {
            if (colorLayoutChildren.get(i) instanceof ColorListEntry colorEntry) {
                colorEntry.setColor(colors.get(i));
            }
        }
    }
}
