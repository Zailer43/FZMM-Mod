package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.containers.ColorOverlay;
import fzmm.zailer.me.utils.list.IListEntry;
import io.wispforest.owo.config.ui.OptionComponentFactory;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ColorPickerComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorRow extends AbstractRow implements IListEntry<Color> {
    public ColorRow(String baseTranslationKey, String id, String tooltipId) {
        this(baseTranslationKey, id, tooltipId, true, true);
    }

    public ColorRow(String baseTranslationKey, String id, String tooltipId, boolean hasResetButton, boolean translate) {
        super(baseTranslationKey, id, tooltipId, hasResetButton, translate);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public Component[] getComponents(String id, String tooltipId) {
        ConfigTextBox colorField = (ConfigTextBox) new ConfigTextBox()
                .horizontalSizing(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(getColorFieldId(id));

        Component box = Components.box(Sizing.fixed(15), Sizing.fixed(15))
                .fill(true)
                .cursorStyle(CursorStyle.HAND)
                .id(getColorPreviewId(id));

        return new Component[]{
                box,
                colorField
        };
    }

    public static String getColorFieldId(String id) {
        return id + "-color";
    }

    public static String getColorPreviewId(String id) {
        return id + "-color-preview";
    }


    /**
     * copy of COLOR of {@link OptionComponentFactory}, since I want to have one just like it,
     * but I don't have an Option<Color> object.
     */
    @SuppressWarnings({"UnstableApiUsage", "ConstantConditions"})
    public static ConfigTextBox setup(FlowLayout rootComponent, String id, Color defaultcolor, boolean withAlpha, @Nullable Consumer<String> changedListener) {
        ConfigTextBox colorField = ConfigTextBoxRow.setup(rootComponent, getColorFieldId(id), id, defaultcolor.asHexString(withAlpha), changedListener);

        colorField.inputPredicate(withAlpha ? s -> s.matches("#[a-zA-Z\\d]{0,8}") : s -> s.matches("#[a-zA-Z\\d]{0,6}"));
        colorField.applyPredicate(withAlpha ? s -> s.matches("#[a-zA-Z\\d]{8}") : s -> s.matches("#[a-zA-Z\\d]{6}"));
        colorField.valueParser(s -> {
                    try {
                        return withAlpha
                                ? Color.ofArgb(Integer.parseUnsignedInt(s.substring(1), 16))
                                : Color.ofRgb(Integer.parseUnsignedInt(s.substring(1), 16));
                    } catch (Exception ignored) {
                        return Color.ofArgb(0);
                    }
                }
        );

        Supplier<Color> valueGetter = () -> (Color) colorField.parsedValue();
        BoxComponent colorPreview = setupColorPreview(id, rootComponent, withAlpha, valueGetter,
                (picker) -> colorField.text(picker.selectedColor().asHexString(withAlpha)));

        colorField.onChanged().subscribe(value -> colorPreview.color(valueGetter.get()));
        colorField.setCursor(0);

        return colorField;
    }


    @SuppressWarnings("ConstantConditions")
    public static BoxComponent setupColorPreview(String id, FlowLayout rootComponent, boolean withAlpha, Supplier<Color> valueGetter, Consumer<ColorPickerComponent> onPress) {
        BoxComponent colorPreview = rootComponent.childById(BoxComponent.class, getColorPreviewId(id));
        BaseFzmmScreen.checkNull(colorPreview, "box", getColorPreviewId(id));

        colorPreview.color(valueGetter.get());

        colorPreview.mouseDown().subscribe((mouseX, mouseY, button) -> {
            ColorOverlay colorOverlay = new ColorOverlay(valueGetter.get(), withAlpha, onPress, colorPreview);
            ((FlowLayout) colorPreview.root()).child(colorOverlay);

            return true;
        });

        return colorPreview;
    }

    public static ColorRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new ColorRow(baseTranslationKey, id, tooltipId);
    }

    public TextFieldWidget getWidget() {
        return this.childById(TextFieldWidget.class, getColorFieldId(this.getId()));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Color getValue() {
        ConfigTextBox colorField = this.childById(ConfigTextBox.class, getColorFieldId(this.getId()));
        return colorField == null ? Color.ofArgb(0) : (Color) colorField.parsedValue();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setValue(Color value) {
        ConfigTextBox colorField = this.childById(ConfigTextBox.class, getColorFieldId(this.getId()));
        if (colorField != null) {
            colorField.setText(value.asHexString(value.alpha() < 1f));
            colorField.setCursor(0);
        }
    }
}
