package fzmm.zailer.me.client.gui.components.containers;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.ColorPickerComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ColorOverlay extends OverlayContainer<EFlowLayout> {
    private final List<FlowLayout> colorsLayouts;
    @Nullable
    private Color selectedColor;
    private static final int WIDTH = 190;
    private static final int HEIGHT = 170;
    private static final int COLOR_SIZE = 16;

    public ColorOverlay(Color color, boolean withAlpha, Consumer<ColorPickerComponent> onConfirm, BoxComponent colorPreview) {
        super(EContainers.verticalFlow(Sizing.content(), Sizing.content()));
        this.colorsLayouts = new ArrayList<>();
        this.selectedColor = null;

        this.addComponents(color, withAlpha, onConfirm, colorPreview);
        this.child.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.child.gap(4);
    }

    protected void addComponents(Color color, boolean withAlpha, Consumer<ColorPickerComponent> onConfirm, BoxComponent colorPreview) {
        FlowLayout colorPickerLayout = this.getColorPickerComponent(color, withAlpha, onConfirm, colorPreview);
        FlowLayout firstRow = EContainers.horizontalFlow(Sizing.content(), Sizing.content());
        firstRow.gap(4);

        ColorPickerComponent picker = colorPickerLayout.childById(ColorPickerComponent.class, "color-picker");
        if (picker == null) {
            FzmmClient.LOGGER.warn("[ColorOverlay] 'color-picker' component not found");
            return;
        }

        firstRow.child(this.getFavoriteColorsLayout(picker));
        firstRow.child(colorPickerLayout);
        firstRow.gap(4);

        this.child.child(firstRow);
        this.child.child(this.getDefaultColorsLayout(picker));

        picker.selectedColor(color);
    }

    public FlowLayout getFavoriteColorsLayout(ColorPickerComponent picker) {
        FzmmConfig.Colors config = FzmmClient.CONFIG.colors;

        EFlowLayout layout = EContainers.verticalFlow(Sizing.fixed(WIDTH), Sizing.fixed(HEIGHT));
        layout.gap(5);
        layout.padding(Insets.of(5));
        layout.surface(layout.styledPanel());
        layout.horizontalAlignment(HorizontalAlignment.CENTER);
        layout.mouseDown().subscribe((input, doubled) -> true);

        UIComponent labelComponent = EComponents.label(net.minecraft.network.chat.Component.translatable("fzmm.gui.colorPicker.title.favorite"))
                .shadow(true)
                .margins(Insets.top(3));

        FlowLayout favoriteColorsComponent = EContainers.ltrTextFlow(Sizing.fill(100), Sizing.content());
        favoriteColorsComponent.children(config.favoriteColors().stream()
                .map(color -> (FlowLayout) this.newColorBox(picker, color))
                .collect(Collectors.toList())
        );
        favoriteColorsComponent.horizontalAlignment(HorizontalAlignment.CENTER);

        ScrollContainer<FlowLayout> favoriteColorsScroll = EContainers.verticalScroll(Sizing.content(), Sizing.fill(75), favoriteColorsComponent);

        ButtonComponent removeColorButton = UIComponents.button(net.minecraft.network.chat.Component.translatable("fzmm.gui.button.remove"),
                this.removeFavoriteExecute(favoriteColorsComponent, config));
        removeColorButton.sizing(Sizing.fixed(50), Sizing.fixed(15))
                .id("remove-favorite-button");

        ButtonComponent addColorButton = UIComponents.button(net.minecraft.network.chat.Component.translatable("fzmm.gui.button.add"),
                this.addFavoriteExecute(picker, favoriteColorsComponent, config));

        addColorButton.sizing(Sizing.fixed(50), Sizing.fixed(15))
                .id("add-favorite-button");

        FlowLayout buttonsLayout = this.getButtonsLayout(removeColorButton, addColorButton);

        layout.children(List.of(labelComponent, favoriteColorsScroll, buttonsLayout));

        return layout;
    }

    private Consumer<ButtonComponent> addFavoriteExecute(ColorPickerComponent picker, FlowLayout favoriteColorsComponent, FzmmConfig.Colors config) {
        return button -> {
            Color selectedColor = picker.selectedColor();
            if (this.getFavoriteList(favoriteColorsComponent).stream().anyMatch(color -> color.equals(selectedColor))) {
                return;
            }

            FlowLayout colorLayout = (FlowLayout) this.newColorBox(picker, selectedColor);

            this.colorsLayouts.add(colorLayout);
            favoriteColorsComponent.child(colorLayout);
            this.updateSelected(selectedColor, colorLayout);

            config.favoriteColors(this.getFavoriteList(favoriteColorsComponent));
            FzmmClient.CONFIG.save();
        };
    }

    private Consumer<ButtonComponent> removeFavoriteExecute(FlowLayout favoriteColorsComponent, FzmmConfig.Colors config) {
        return button -> {
            if (this.selectedColor == null) {
                return;
            }

            List<Color> updatedFavoriteColors = new ArrayList<>(config.favoriteColors());
            if (!updatedFavoriteColors.removeIf(color -> color.equals(this.selectedColor))) {
                return;
            }

            List<UIComponent> favoriteColorsComponentList = List.copyOf(favoriteColorsComponent.children());
            for (var favoriteColor : favoriteColorsComponentList) {
                if (favoriteColor instanceof FlowLayout colorLayout &&
                        colorLayout.children().get(0) instanceof BoxComponent boxComponent &&
                        boxComponent.startColor().get() == this.selectedColor) {

                    favoriteColorsComponent.removeChild(favoriteColor);
                    this.colorsLayouts.remove(favoriteColor);
                }
            }

            this.updateSelected(null, null);

            config.favoriteColors(updatedFavoriteColors);
            FzmmClient.CONFIG.save();
        };
    }

    private List<Color> getFavoriteList(FlowLayout favoriteColorsComponent) {
        List<Color> colorList = new ArrayList<>();

        for (var component : favoriteColorsComponent.children()) {
            if (component instanceof FlowLayout layout && layout.children().get(0) instanceof BoxComponent boxComponent) {
                colorList.add(boxComponent.startColor().get());
            }
        }

        return colorList;
    }

    public UIComponent newColorBox(ColorPickerComponent picker, Color color) {
        UIComponent boxComponent = UIComponents.box(Sizing.fixed(COLOR_SIZE), Sizing.fixed(COLOR_SIZE))
                .color(color)
                .fill(true)
                .margins(Insets.of(1))
                .cursorStyle(CursorStyle.HAND);

        FlowLayout colorLayout = EContainers.horizontalFlow(Sizing.fixed(COLOR_SIZE + 2), Sizing.fixed(COLOR_SIZE + 2));
        colorLayout.padding(Insets.of(1));
        colorLayout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        colorLayout.child(boxComponent);

        boxComponent.mouseDown().subscribe((input, doubled) -> {
            picker.selectedColor(color);

            this.updateSelected(color, colorLayout);

            return true;
        });

        this.colorsLayouts.add(colorLayout);

        return colorLayout;
    }

    private void updateSelected(@Nullable Color color, @Nullable FlowLayout colorLayout) {
        for (var layout : this.colorsLayouts) {
            layout.surface(Surface.BLANK);
        }

        this.selectedColor = color;

        if (color != null &&
                colorLayout != null &&
                colorLayout.children().get(0) instanceof BoxComponent boxComponent &&
                boxComponent.startColor().get().equals(color)) {

            colorLayout.surface(Surface.outline(0xFFFFFFFF));
        }
    }

    public FlowLayout getColorPickerComponent(Color color, boolean withAlpha, Consumer<ColorPickerComponent> onConfirm, BoxComponent colorPreview) {
        EFlowLayout layout = EContainers.verticalFlow(Sizing.fixed(WIDTH), Sizing.fixed(HEIGHT));
        layout.gap(5)
                .padding(Insets.of(5))
                .surface(layout.styledPanel())
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .mouseDown().subscribe((input, doubled) -> true);

        UIComponent labelComponent = EComponents.label(net.minecraft.network.chat.Component.translatable("fzmm.gui.colorPicker.title.picker"));

        ColorPickerComponent picker = (ColorPickerComponent) new ColorPickerComponent()
                .selectedColor(color)
                .showAlpha(withAlpha)
                .sizing(Sizing.fixed(160), Sizing.fixed(100))
                .id("color-picker");

        BoxComponent currentColor = (BoxComponent) UIComponents.box(Sizing.fixed(80), Sizing.fixed(15))
                .fill(true)
                .color(color)
                .id("current-color");

        FlowLayout colorsLayout = EContainers.horizontalFlow(Sizing.content(), Sizing.content()).child(
                UIComponents.box(Sizing.fixed(80), Sizing.fixed(15))
                        .fill(true)
                        .color(color)
        ).child(currentColor);

        assert this.child.parent() != null;
        ButtonComponent cancelButton = UIComponents.button(net.minecraft.network.chat.Component.translatable("fzmm.gui.colorPicker.cancel"), buttonComponent -> this.child.parent().remove());
        cancelButton.sizing(Sizing.fixed(50), Sizing.fixed(15))
                .id("cancel-button");

        ButtonComponent confirmButton = UIComponents.button(net.minecraft.network.chat.Component.translatable("fzmm.gui.colorPicker.confirm"), buttonComponent -> {
            onConfirm.accept(picker);
            colorPreview.color(picker.selectedColor());
            this.child.parent().remove();
        });
        confirmButton.sizing(Sizing.fixed(50), Sizing.fixed(15))
                .id("confirm-button");

        FlowLayout buttonsLayout = this.getButtonsLayout(cancelButton, confirmButton);

        picker.onChanged().subscribe(selectedColor -> {
            this.selectedColor = selectedColor;
            currentColor.color(selectedColor);
        });

        List<UIComponent> components = List.of(labelComponent, picker, colorsLayout, buttonsLayout);
        layout.children(components);

        return layout;
    }

    private FlowLayout getButtonsLayout(UIComponent... components) {
        return (FlowLayout) EContainers.horizontalFlow(Sizing.fill(100), Sizing.content())
                .children(Arrays.asList(components))
                .gap(10)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .positioning(Positioning.relative(0, 98));
    }

    private FlowLayout getDefaultColorsLayout(ColorPickerComponent picker) {
        FlowLayout result = EContainers.verticalFlow(Sizing.content(), Sizing.content());
        result.gap(4);

        int width = WIDTH * 2 + 4;
        result.child(this.getDyeColorsLayout(picker, width));
        result.child(this.getFormattingLayout(picker, width));

        for (var child : result.children()) {
            if (child instanceof EFlowLayout flowLayout) {
                flowLayout.padding(Insets.of(5))
                        .surface(flowLayout.styledPanel())
                        .horizontalAlignment(HorizontalAlignment.CENTER);
            }
        }
        return result;
    }

    private FlowLayout getDyeColorsLayout(ColorPickerComponent picker, int width) {
        DyeColor[] defaultDyeColorArray = FzmmUtils.getDyeColorsInOrder();
        List<Tuple<Color, UIComponent>> dyeColorsComponents = new ArrayList<>();

        for (DyeColor dyeColor : defaultDyeColorArray) {
            Item dyeItem = Items.AIR;

            for (var item : BuiltInRegistries.ITEM.stream().toList()) {
                if (item instanceof DyeItem dyeItem1 && dyeItem1.getDyeColor() == dyeColor) {
                    dyeItem = item;
                    break;
                }
            }

            dyeColorsComponents.add(new Tuple<>(Color.ofDye(dyeColor), UIComponents.item(dyeItem.getDefaultInstance())));
        }

        List<UIComponent> dyeComponents = this.getColorsComponentsWithIcon(picker, dyeColorsComponents);

        FlowLayout dyeLayout = EContainers.ltrTextFlow(Sizing.fixed(width), Sizing.content());
        dyeLayout.children(dyeComponents);

        return dyeLayout;
    }

    private FlowLayout getFormattingLayout(ColorPickerComponent picker, int width) {
        ChatFormatting[] defaultFormattingArray = FzmmUtils.getFormattingColorsInOrder();

        List<UIComponent> formattingComponents = new ArrayList<>();

        for (ChatFormatting formatting : defaultFormattingArray) {
            if (formatting.getColor() == null) {
                continue;
            }

            String colorCode = String.valueOf(formatting.getChar());
            FlowLayout colorLayout = (FlowLayout) this.newColorBox(picker, Color.ofFormatting(formatting));
            colorLayout.tooltip(net.minecraft.network.chat.Component.literal("&" + colorCode));
            formattingComponents.add(colorLayout);
        }

        FlowLayout formattingLayout = EContainers.ltrTextFlow(Sizing.fixed(width), Sizing.content());
        formattingLayout.children(formattingComponents);

        return formattingLayout;
    }

    private List<UIComponent> getColorsComponentsWithIcon(ColorPickerComponent picker, List<Tuple<Color, UIComponent>> colors) {
        List<UIComponent> result = new ArrayList<>();

        for (var entry : colors) {
            StackLayout colorStack = UIContainers.stack(Sizing.content(), Sizing.content());
            FlowLayout colorLayout = (FlowLayout) this.newColorBox(picker, entry.getA());

            UIComponent colorIconComponent = entry.getB();

            colorStack.child(colorLayout);
            colorStack.child(colorIconComponent);

            colorIconComponent.mouseDown().subscribe(colorLayout::onMouseDown);
            colorIconComponent.cursorStyle(CursorStyle.HAND);

            colorStack.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            result.add(colorStack);
        }

        return result;
    }
}
