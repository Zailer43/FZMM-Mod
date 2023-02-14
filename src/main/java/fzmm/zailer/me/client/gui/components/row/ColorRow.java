package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.config.ui.OptionComponentFactory;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.ColorPickerComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorRow extends AbstractRow {
    public ColorRow(String baseTranslationKey, String id, String tooltipId) {
        this(baseTranslationKey, id, tooltipId, true);
    }

    public ColorRow(String baseTranslationKey, String id, String tooltipId, boolean hasResetButton) {
        super(baseTranslationKey, id, tooltipId, hasResetButton);
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

        if (!(MinecraftClient.getInstance().currentScreen instanceof BaseFzmmScreen baseFzmmScreen))
            return colorField;

        BoxComponent colorPreview = rootComponent.childById(BoxComponent.class, getColorPreviewId(id));
        BaseFzmmScreen.checkNull(colorPreview, "box", getColorPreviewId(id));

        UIModel model = baseFzmmScreen.getModel();

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
        colorPreview.color(valueGetter.get());

        colorField.onChanged().subscribe(value -> colorPreview.color(valueGetter.get()));

        colorPreview.mouseDown().subscribe((mouseX, mouseY, button) -> {
            ((FlowLayout) colorPreview.root()).child(Containers.overlay(
                    model.expandTemplate(
                            FlowLayout.class,
                            "color-picker-panel",
                            Map.of("color", valueGetter.get().asHexString(withAlpha), "with-alpha", String.valueOf(withAlpha))
                    ).<FlowLayout>configure(flowLayout -> {
                        var picker = flowLayout.childById(ColorPickerComponent.class, "color-picker");
                        var previewBox = flowLayout.childById(BoxComponent.class, "current-color");

                        picker.onChanged().subscribe(previewBox::color);

                        flowLayout.childById(ButtonComponent.class, "confirm-button").onPress(confirmButton -> {
                            colorField.text(picker.selectedColor().asHexString(withAlpha));
                            flowLayout.parent().remove();
                        });

                        flowLayout.childById(ButtonComponent.class, "cancel-button").onPress(cancelButton -> flowLayout.parent().remove());
                    })
            ));

            return true;
        });

        return colorField;
    }

    public static ColorRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new ColorRow(baseTranslationKey, id, tooltipId);
    }

    @SuppressWarnings("UnstableApiUsage")
    public void setColor(Color color) {
        ConfigTextBox colorField = this.childById(ConfigTextBox.class, getColorFieldId(this.getId()));
        if (colorField != null)
            colorField.setText(color.asHexString(color.alpha() < 1f));
    }

    @SuppressWarnings("UnstableApiUsage")
    public Color getColor() {
        ConfigTextBox colorField = this.childById(ConfigTextBox.class, getColorFieldId(this.getId()));
        return colorField == null ? Color.ofArgb(0) : (Color) colorField.parsedValue();
    }
}
