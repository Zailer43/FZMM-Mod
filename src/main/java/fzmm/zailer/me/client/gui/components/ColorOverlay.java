package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.containers.VerticalGridLayout;
import fzmm.zailer.me.config.FzmmConfig;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ColorOverlay extends OverlayContainer<GridLayout> {
    private final List<FlowLayout> favoriteColors;
    @Nullable
    private Color selectedColor;
    private static final int WIDTH = 170;
    private static final int HEIGHT = 170;

    public ColorOverlay(Color color, boolean withAlpha, Consumer<ColorPickerComponent> onConfirm, BoxComponent colorPreview) {
        super(Containers.grid(Sizing.fixed(WIDTH * 2 + 15), Sizing.fixed(HEIGHT), 1, 2));
        this.favoriteColors = new ArrayList<>();
        this.selectedColor = null;

        this.addComponents(color, withAlpha, onConfirm, colorPreview);
        this.child.alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    }

    protected void addComponents(Color color, boolean withAlpha, Consumer<ColorPickerComponent> onConfirm, BoxComponent colorPreview) {
        FlowLayout colorPickerLayout = this.getColorPickerComponent(color, withAlpha, onConfirm, colorPreview);
        colorPickerLayout.margins(Insets.horizontal(15));
        this.child.child(this.getFavoriteColorsLayout(colorPickerLayout), 0, 0);
        this.child.child(colorPickerLayout, 0, 1);
    }

    public FlowLayout getFavoriteColorsLayout(FlowLayout colorPickerLayout) {
        FzmmConfig.Colors config = FzmmClient.CONFIG.colors;

        FlowLayout layout = Containers.verticalFlow(Sizing.fixed(WIDTH), Sizing.fixed(HEIGHT));
        layout.gap(5);
        layout.padding(Insets.of(5));
        layout.surface(Surface.DARK_PANEL);
        layout.horizontalAlignment(HorizontalAlignment.CENTER);
        layout.mouseDown().subscribe((mouseX, mouseY, button) -> true);

        int boxSize = 16;
        this.favoriteColors.clear();

        Component labelComponent = Components.label(Text.translatable("fzmm.gui.colorPicker.title.favorite"))
                .shadow(true)
                .margins(Insets.top(3));

        ColorPickerComponent picker = colorPickerLayout.childById(ColorPickerComponent.class, "color-picker");
        if (picker == null) {
            FzmmClient.LOGGER.warn("[ColorOverlay] 'color-picker' component not found");
            return layout;
        }

        VerticalGridLayout favoriteColorsComponent = new VerticalGridLayout(Sizing.fill(100), Sizing.content(), 20, 0, boxSize + 2, boxSize + 2);
        favoriteColorsComponent.children(config.favoriteColors().stream()
                .map(color -> {
                    FlowLayout colorLayout = (FlowLayout) this.getFavoriteColorBox(picker, color, boxSize);
                    this.favoriteColors.add(colorLayout);
                    return colorLayout;
                })
                .collect(Collectors.toList())
        );
        favoriteColorsComponent.horizontalAlignment(HorizontalAlignment.CENTER);

        ScrollContainer<VerticalGridLayout> favoriteColorsScroll = Containers.verticalScroll(Sizing.content(), Sizing.fill(75), favoriteColorsComponent);
        favoriteColorsScroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));

        ButtonComponent removeColorButton = Components.button(Text.translatable("fzmm.gui.button.remove"), button -> {
            if (this.selectedColor == null)
                return;

            for (var favoriteColor : favoriteColorsComponent.children()) {
                if (favoriteColor instanceof FlowLayout colorLayout &&
                        colorLayout.children().get(0) instanceof BoxComponent boxComponent &&
                        boxComponent.startColor().get() == this.selectedColor) {

                    favoriteColorsComponent.removeChild(favoriteColor);
                    this.favoriteColors.remove(favoriteColor);
                }
            }
            this.updateSelected(null);

            config.favoriteColors(this.getFavoriteList());
            FzmmClient.CONFIG.save();
        });
        removeColorButton.sizing(Sizing.fixed(50), Sizing.fixed(15))
                .zIndex(300)
                .id("remove-favorite-button");

        ButtonComponent addColorButton = Components.button(Text.translatable("fzmm.gui.button.add"),
                button -> {
                    Color selectedColor = picker.selectedColor();
                    FlowLayout colorLayout = (FlowLayout) this.getFavoriteColorBox(picker, picker.selectedColor(), boxSize);
                    if (!this.favoriteColors.contains(colorLayout)) {
                        this.favoriteColors.add(colorLayout);
                        favoriteColorsComponent.child(colorLayout);
                        this.updateSelected(selectedColor);

                        config.favoriteColors(this.getFavoriteList());
                        FzmmClient.CONFIG.save();
                    }
                });

        addColorButton.sizing(Sizing.fixed(50), Sizing.fixed(15))
                .zIndex(300)
                .id("add-favorite-button");

        FlowLayout buttonsLayout = this.getButtonsLayout(removeColorButton, addColorButton);

        layout.children(List.of(labelComponent, favoriteColorsScroll, buttonsLayout));

        return layout;
    }

    private List<Color> getFavoriteList() {
        List<Color> colorList = new ArrayList<>();

        for (var flowLayout : this.favoriteColors) {
            if (flowLayout.children().get(0) instanceof BoxComponent boxComponent) {
                colorList.add(boxComponent.startColor().get());
            }
        }

        return colorList;
    }

    public Component getFavoriteColorBox(ColorPickerComponent picker, Color color, int boxSize) {
        Component boxComponent = Components.box(Sizing.fixed(boxSize), Sizing.fixed(boxSize))
                .color(color)
                .fill(true)
                .margins(Insets.of(1))
                .cursorStyle(CursorStyle.HAND);

        FlowLayout colorLayout = Containers.horizontalFlow(Sizing.fixed(boxSize + 2), Sizing.fixed(boxSize + 2));
        colorLayout.padding(Insets.of(1));
        colorLayout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        colorLayout.child(boxComponent);

        boxComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            picker.selectedColor(color);

            this.updateSelected(color);

            return true;
        });

        for (var flowLayout : this.favoriteColors) {
            if (flowLayout.children().get(0) instanceof BoxComponent component && component.startColor().get().equals(color)) {
                return flowLayout;
            }
        }

        return colorLayout;
    }

    private void updateSelected(@Nullable Color color) {
        for (var layout : this.favoriteColors) {
            layout.surface(Surface.outline(0x00000000));
        }

        this.selectedColor = color;

        if (color == null)
            return;

        for (var flowLayout : this.favoriteColors) {
            if (flowLayout.children().get(0) instanceof BoxComponent component && component.startColor().get().equals(color)) {
                flowLayout.surface(Surface.outline(0xFFFFFFFF));
            }
        }
    }

    public FlowLayout getColorPickerComponent(Color color, boolean withAlpha, Consumer<ColorPickerComponent> onConfirm, BoxComponent colorPreview) {
        FlowLayout layout = Containers.verticalFlow(Sizing.fixed(WIDTH), Sizing.fixed(HEIGHT));
        layout.gap(5);
        layout.padding(Insets.of(5));
        layout.surface(Surface.DARK_PANEL);
        layout.horizontalAlignment(HorizontalAlignment.CENTER);
        layout.mouseDown().subscribe((mouseX, mouseY, button) -> true);

        Component labelComponent = Components.label(Text.translatable("fzmm.gui.colorPicker.title.picker"))
                .shadow(true)
                .margins(Insets.top(3));

        ColorPickerComponent picker = (ColorPickerComponent) new ColorPickerComponent()
                .selectedColor(color)
                .showAlpha(withAlpha)
                .sizing(Sizing.fixed(160), Sizing.fixed(100))
                .id("color-picker");

        BoxComponent currentColor = (BoxComponent) Components.box(Sizing.fixed(80), Sizing.fixed(15))
                .fill(true)
                .color(color)
                .id("current-color");

        FlowLayout colorsLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content()).child(
                Components.box(Sizing.fixed(80), Sizing.fixed(15))
                        .fill(true)
                        .color(color)
        ).child(currentColor);

        assert this.child.parent() != null;
        ButtonComponent cancelButton = Components.button(Text.translatable("fzmm.gui.colorPicker.cancel"), buttonComponent -> this.child.parent().remove());
        cancelButton.sizing(Sizing.fixed(50), Sizing.fixed(15))
                .zIndex(300)
                .id("cancel-button");

        ButtonComponent confirmButton = Components.button(Text.translatable("fzmm.gui.colorPicker.confirm"), buttonComponent -> {
            onConfirm.accept(picker);
            colorPreview.color(picker.selectedColor());
            this.child.parent().remove();
        });
        confirmButton.sizing(Sizing.fixed(50), Sizing.fixed(15))
                .zIndex(300)
                .id("confirm-button");

        FlowLayout buttonsLayout = this.getButtonsLayout(cancelButton, confirmButton);

        picker.onChanged().subscribe(selectedColor -> {
            this.selectedColor = selectedColor;
            currentColor.color(selectedColor);
        });

        List<Component> components = List.of(labelComponent, picker, colorsLayout, buttonsLayout);
        layout.children(components);

        return layout;
    }

    private FlowLayout getButtonsLayout(Component... components) {
        return (FlowLayout) Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                .children(Arrays.asList(components))
                .gap(10)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .positioning(Positioning.relative(0, 98));
    }
}
