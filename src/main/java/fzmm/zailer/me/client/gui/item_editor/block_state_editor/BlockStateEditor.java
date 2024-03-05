package fzmm.zailer.me.client.gui.item_editor.block_state_editor;

import fzmm.zailer.me.builders.BlockStateItemBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import fzmm.zailer.me.mixin.block_state_editor.VerticallyAttachableBlockItemAccessor;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.*;

public class BlockStateEditor implements IItemEditorScreen {
    private static final String BLOCK_STATE_TRANSLATION_KEY = "fzmm.gui.itemEditor.block_state.state.";
    // black magic to prevent scrolling to the top of the screen when
    // updating because of layout.clearChildren() + layout.children(children)
    private final HashMap<Property<?>, HashMap<String, FlowLayout>> statesLayoutOfProperties = new HashMap<>();
    private RequestedItem blockRequested = null;
    private List<RequestedItem> requestedItems = null;
    private BlockStateItemBuilder blockBuilder = BlockStateItemBuilder.builder();
    private FlowLayout blockPreviewLayout;
    private FlowLayout contentLayout;
    private ButtonComponent blockButton;
    private ButtonComponent wallBlockButton;
    private boolean wallBlockSelected;
    private UIModel uiModel;

    @Override
    public List<RequestedItem> getRequestedItems() {
        if (this.requestedItems != null)
            return this.requestedItems;

        this.blockRequested = new RequestedItem(
                itemStack -> {
                    Item item = itemStack.getItem();
                    if (item instanceof BlockItem blockItem) {
                        if (this.hasBlockState(blockItem.getBlock()))
                            return true;
                    }

                    if (item instanceof VerticallyAttachableBlockItem wallItem) {
                        Block wallBlock = ((VerticallyAttachableBlockItemAccessor) wallItem).getWallBlock();

                        return this.hasBlockState(wallBlock);
                    }

                    return false;
                },
                this::selectItemAndUpdateParameters,
                null,
                Text.translatable("fzmm.gui.itemEditor.block_state.title"),
                true
        );

        this.requestedItems = List.of(this.blockRequested);
        return this.requestedItems;
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.STONE_STAIRS.getDefaultStack();
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        this.uiModel = this.getUIModel().orElseThrow();

        this.blockPreviewLayout = editorLayout.childById(FlowLayout.class, "block-preview-layout");
        BaseFzmmScreen.checkNull(this.blockPreviewLayout, "flow-layout", "block-preview-layout");

        this.contentLayout = editorLayout.childById(FlowLayout.class, "content");
        BaseFzmmScreen.checkNull(this.contentLayout, "flow-layout", "content");

        this.blockButton = editorLayout.childById(ButtonComponent.class, "block-button");
        BaseFzmmScreen.checkNull(this.blockButton, "button", "block-button");
        this.blockButton.onPress(buttonComponent -> {
            this.wallBlockSelected = false;
            this.wallBlockButton.active = true;
            this.blockButton.active = false;
            this.updateBlockStateContent();
            this.updatePreview();
        });

        this.wallBlockButton = editorLayout.childById(ButtonComponent.class, "wall-block-button");
        BaseFzmmScreen.checkNull(this.wallBlockButton, "button", "wall-block-button");
        this.wallBlockButton.onPress(buttonComponent -> {
            this.wallBlockSelected = true;
            this.wallBlockButton.active = false;
            this.blockButton.active = true;
            this.updateBlockStateContent();
            this.updatePreview();
        });

        this.wallBlockSelected = false;
        this.blockBuilder.item(Items.AIR);

        return editorLayout;
    }

    @Override
    public String getId() {
        return "block_state";
    }

    @Override
    public void updateItemPreview() {
        this.blockRequested.setStack(this.blockBuilder.get());
        this.blockRequested.updatePreview();
        this.updatePreview();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        this.blockBuilder = this.blockBuilder.of(stack);
        this.blockButton.active = this.blockBuilder.blockState().isPresent() && this.wallBlockSelected;
        this.wallBlockButton.active = this.blockBuilder.wallBlockState().isPresent() && !this.wallBlockSelected;

        this.updateBlockStateContent();
        this.updatePreview();
    }

    private boolean hasBlockState(Block block) {
        return !block.getDefaultState().getProperties().isEmpty();
    }

    private Optional<BlockState> getBlockState() {
        if (this.wallBlockSelected)
            return this.blockBuilder.wallBlockState();

        return this.blockBuilder.blockState();
    }

    public void updateBlockStateContent() {
        this.contentLayout.clearChildren();
        this.statesLayoutOfProperties.clear();
        List<Component> components = new ArrayList<>();
        Optional<BlockState> blockStateOptional = this.getBlockState();

        if (blockStateOptional.isEmpty() || blockStateOptional.get().getProperties().isEmpty()) {
            components.add(Components.label(Text.translatable("fzmm.gui.itemEditor.block_state.label.empty"))
                    .margins(Insets.of(10).withLeft(40))
            );
            this.contentLayout.children(components);
            return;
        }

        BlockState blockState = blockStateOptional.get();
        List<Property<?>> propertyList = blockState.getProperties().stream().sorted(Comparator.comparing(Property::getName)).toList();
        for (var property : propertyList) {
            this.resetStateLayoutsOfProperty(property);

            FlowLayout valuesLayout = Containers.ltrTextFlow(Sizing.fill(100), Sizing.content());
            valuesLayout.children(this.getValuesLayout(blockState, property));

            LabelComponent propertyLabel = Components.label(Text.translatable(BLOCK_STATE_TRANSLATION_KEY + property.getName()));
            String propertyName = property.getName();
            this.setTranslation(propertyLabel, propertyName, propertyName, true);

            FlowLayout layout = (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content())
                    .gap(10)
                    .child(propertyLabel)
                    .child(valuesLayout)
                    .horizontalAlignment(HorizontalAlignment.LEFT)
                    .padding(Insets.of(10));
            layout.mouseEnter().subscribe(() -> layout.surface(Surface.flat(0x20000000)));
            layout.mouseLeave().subscribe(() -> layout.surface(Surface.flat(0)));

            layout.horizontalAlignment(HorizontalAlignment.CENTER);

            components.add(layout);
        }

        this.contentLayout.children(components);
    }

    public void resetStateLayoutsOfProperty(Property<?> property) {
        HashMap<String, FlowLayout> stateLayoutList = new HashMap<>();
        this.statesLayoutOfProperties.put(property, stateLayoutList);
        stateLayoutList.put("default", Containers.verticalFlow(Sizing.content(), Sizing.content()));
        for (var value : property.getValues())
            stateLayoutList.put(value.toString().toLowerCase(), Containers.verticalFlow(Sizing.content(), Sizing.content()));
    }

    public void updatePreview() {
        this.getBlockState().ifPresent(blockState -> {
            Component blockPreviewComponent = Components.block(blockState, this.blockBuilder.nbt())
                    .sizing(Sizing.fixed(30), Sizing.fixed(30));
            this.blockPreviewLayout.clearChildren();
            this.blockPreviewLayout.child(blockPreviewComponent);
        });
    }

    private List<Component> getValuesLayout(BlockState blockState, Property<?> property) {
        String propertyName = property.getName();
        List<Component> valueLayouts = new ArrayList<>();

        String defaultValueName = blockState.getBlock().getDefaultState().get(property).toString().toLowerCase();
        valueLayouts.add(this.getValueLayout(defaultValueName, property, propertyName, true));

        for (var value : property.getValues()) {
            String valueName = value.toString().toLowerCase();
            valueLayouts.add(this.getValueLayout(valueName, property, propertyName, false));
        }
        return valueLayouts;
    }

    private FlowLayout getValueLayout(String valueName, Property<?> property, String propertyName, boolean isDefault) {
        HashMap<String, String> parameters = new HashMap<>();
        String labelName = valueName;
        String key = propertyName + "-" + labelName;
        String labelId = key + "-label";
        String labelKey = propertyName + "." + labelName;
        String valueParameter = valueName;
        if (isDefault) {
            labelName = "default";
            key = propertyName + "-default";
            labelId = key + "-label";
            labelKey = "default";
            valueParameter = "default";
        }
        parameters.put("property", propertyName);
        parameters.put("value", valueParameter);
        FlowLayout valueLayout = this.uiModel.expandTemplate(FlowLayout.class, "block-state-button", parameters);

        this.setStateLayout(key, labelName, property, valueLayout, isDefault);

        LabelComponent labelComponent = valueLayout.childById(LabelComponent.class, labelId);
        BaseFzmmScreen.checkNull(labelComponent, "label", labelId);
        this.setTranslation(labelComponent, labelKey, labelName, false);

        return valueLayout;
    }

    private void setStateLayout(String translationKey, String valueName, Property<?> property, FlowLayout valueLayout, boolean isDefault) {
        String stateLayoutId = translationKey + "-state-layout";
        FlowLayout stateLayout = valueLayout.childById(FlowLayout.class, stateLayoutId);
        BaseFzmmScreen.checkNull(stateLayout, "flow-layout", stateLayoutId);
        stateLayout.child(this.statesLayoutOfProperties.get(property).get(valueName));
        this.updateStateLayout(property, valueName, isDefault);
    }

    public void updatePropertiesContent() {
        Optional<BlockState> blockStateOptional = this.getBlockState();
        if (blockStateOptional.isEmpty())
            return;

        for (var property : this.statesLayoutOfProperties.keySet()) {
            HashMap<String, FlowLayout> statesLayoutOfProperty = this.statesLayoutOfProperties.get(property);
            for (var valueName : statesLayoutOfProperty.keySet())
                this.updateStateLayout(property, valueName, valueName.equals("default"));
        }

        this.updateItemPreview();
    }

    private void updateStateLayout(Property<?> property, String valueName, boolean isDefault) {
        Optional<BlockState> blockStateOptional = this.getBlockState();
        if (blockStateOptional.isEmpty())
            return;
        BlockState blockState = blockStateOptional.get();

        String value = valueName;
        if (isDefault)
            value = blockState.getBlock().getDefaultState().get(property).toString().toLowerCase();

        String propertyName = property.getName();
        Component blockComponent = Components.block(BlockItem.with(blockState, property, value), this.blockBuilder.nbt())
                .sizing(Sizing.fixed(40), Sizing.fixed(40));

        FlowLayout stateLayout = this.statesLayoutOfProperties.get(property).get(valueName);
        stateLayout.clearChildren();
        stateLayout.child(blockComponent);
        boolean propertyUsed = this.blockBuilder.contains(propertyName);

        boolean waterlogged = property == Properties.WATERLOGGED && valueName.equals("true")
                || (property != Properties.WATERLOGGED && this.blockBuilder.isState(Properties.WATERLOGGED.getName(), "true"));

        // there is always one active, if the property is not in use, it is the default, otherwise it is the selected one
        if ((isDefault && !propertyUsed) || (this.blockBuilder.isState(propertyName, valueName) && propertyUsed && !isDefault)) {
            stateLayout.surface(Surface.flat(waterlogged ? 0x6050A4AF : 0x6050af68));
        } else {
            int disabledColor = waterlogged ? 0x602A4CD5 : 0;
            stateLayout.surface(Surface.flat(disabledColor))
                    .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    .cursorStyle(CursorStyle.HAND)
                    .sizing(Sizing.fixed(40), Sizing.fixed(40));
            blockComponent.mouseDown().subscribe((mouseX, mouseY, button) -> this.onStateExecute(property, valueName, isDefault));
        }
    }

    private boolean onStateExecute(Property<?> property, String valueName, boolean isDefault) {
        String propertyName = property.getName();

        if (isDefault)
            this.blockBuilder.remove(propertyName);
        else
            this.blockBuilder.add(propertyName, valueName);

        this.updatePropertiesContent();
        return true;
    }

    private void setTranslation(LabelComponent labelComponent, String key, String value, boolean bold) {
        String translationKey = BLOCK_STATE_TRANSLATION_KEY + key;
        MutableText translation = Text.translatable(translationKey);

        if (translation.getString().equals(translationKey)) {
            labelComponent.text(Text.literal(value).setStyle(Style.EMPTY.withBold(bold)));
        } else {
            labelComponent.text(translation.setStyle(Style.EMPTY.withBold(bold)));
            labelComponent.tooltip(Text.of(value));
        }
    }
}