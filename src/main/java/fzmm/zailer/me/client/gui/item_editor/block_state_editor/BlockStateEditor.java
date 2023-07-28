package fzmm.zailer.me.client.gui.item_editor.block_state_editor;

import fzmm.zailer.me.builders.BlockStateItemBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BlockStateEditor implements IItemEditorScreen {
    private static final String BLOCK_STATE_TRANSLATION_KEY = "fzmm.gui.itemEditor.block_state.state.";
    private static final String BLOCK_PREVIEW_LAYOUT_ID = "block-preview-layout";
    private static final String CONTENT_ID = "content";
    private static final String BLOCK_STATE_BUTTON_ID = "block-state-button";
    // black magic to prevent scrolling to the top of the screen when
    // updating because of layout.clearChildren() + layout.children(children)
    private final HashMap<Property<?>, HashMap<String, FlowLayout>> statesLayoutOfProperties = new HashMap<>();
    private RequestedItem blockRequested = null;
    private List<RequestedItem> requestedItems = null;
    private BlockStateItemBuilder blockBuilder = BlockStateItemBuilder.builder();
    private FlowLayout blockPreviewLayout;
    private FlowLayout contentLayout;
    private UIModel uiModel;

    @Override
    public List<RequestedItem> getRequestedItems(Consumer<ItemStack> firstItemSetter) {
        if (this.requestedItems != null)
            return this.requestedItems;

        this.blockRequested = new RequestedItem(
                itemStack -> itemStack.getItem() instanceof BlockItem blockItem && !blockItem.getBlock().getDefaultState().getProperties().isEmpty(),
                this::setItem,
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
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, int x, int y, int width, int height) {
        UIModel uiModel = BaseUIModelScreen.DataSource.asset(new Identifier(FzmmClient.MOD_ID, "item_editor/block_state_editor")).get();
        if (uiModel == null) {
            FzmmClient.LOGGER.error("[BlockStateEditor] Failed to load UIModel");
            return null;
        }
        this.uiModel = uiModel;

        assert MinecraftClient.getInstance().world != null;
        FlowLayout rootComponent = uiModel.createAdapterWithoutScreen(x, y, width, height, FlowLayout.class).rootComponent;

        this.blockPreviewLayout = rootComponent.childById(FlowLayout.class, BLOCK_PREVIEW_LAYOUT_ID);
        BaseFzmmScreen.checkNull(this.blockPreviewLayout, "flow-layout", BLOCK_PREVIEW_LAYOUT_ID);

        this.contentLayout = rootComponent.childById(FlowLayout.class, CONTENT_ID);
        BaseFzmmScreen.checkNull(this.contentLayout, "flow-layout", CONTENT_ID);

        return rootComponent;
    }

    @Override
    public String getId() {
        return "block_state";
    }

    @Override
    public void setItem(ItemStack stack) {
        boolean otherItem = stack.getItem() != this.blockBuilder.item();
        this.blockBuilder = this.blockBuilder.of(stack);
        this.blockRequested.setStack(stack);
        this.blockRequested.updatePreview();

        if (otherItem || stack.isEmpty())
            this.updateBlockStateContent();
        this.updatePreview();
    }

    public void updateBlockStateContent() {
        this.contentLayout.clearChildren();
        this.statesLayoutOfProperties.clear();
        List<Component> components = new ArrayList<>();
        Optional<BlockState> blockStateOptional = this.blockBuilder.blockState();

        if (blockStateOptional.isEmpty() || blockStateOptional.get().getProperties().isEmpty()) {
            components.add(Components.label(Text.translatable("fzmm.gui.itemEditor.block_state.label.empty")));
            this.contentLayout.children(components);
            return;
        }

        BlockState blockState = blockStateOptional.get();
        for (var property : blockState.getProperties()) {
            this.resetStateLayoutsOfProperty(property);

            FlowLayout valuesLayout = Containers.ltrTextFlow(Sizing.fill(100), Sizing.content());
            valuesLayout.children(this.getValuesLayout(blockState, property));

            LabelComponent propertyLabel = Components.label(Text.translatable(BLOCK_STATE_TRANSLATION_KEY + property.getName()));
            String propertyName = property.getName();
            this.setTranslation(propertyLabel, propertyName, propertyName);

            FlowLayout layout = (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content())
                    .gap(10)
                    .child(propertyLabel)
                    .child(valuesLayout)
                    .horizontalAlignment(HorizontalAlignment.LEFT)
                    .padding(Insets.of(10));
            layout.mouseEnter().subscribe(() -> layout.surface(Surface.flat(0x20000000)));
            layout.mouseLeave().subscribe(() -> layout.surface(Surface.flat(0)));

            components.add(layout);
        }

        this.contentLayout.gap(8);
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
        this.blockBuilder.blockState().ifPresent(blockState -> {
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
        String propertyParameter = propertyName;
        String valueParameter = valueName;
        if (isDefault) {
            labelName = "default";
            key = "default-default";
            labelId = key + "-label";
            labelKey = "default";
            propertyParameter = "default";
            valueParameter = "default";
        }
        parameters.put("property", propertyParameter);
        parameters.put("value", valueParameter);
        FlowLayout valueLayout = this.uiModel.expandTemplate(FlowLayout.class, BLOCK_STATE_BUTTON_ID, parameters);

        this.setStateLayout(key, labelName, property, valueLayout, isDefault);

        LabelComponent labelComponent = valueLayout.childById(LabelComponent.class, labelId);
        BaseFzmmScreen.checkNull(labelComponent, "label", labelId);
        this.setTranslation(labelComponent, labelKey, labelName);

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
        Optional<BlockState> blockStateOptional = this.blockBuilder.blockState();
        if (blockStateOptional.isEmpty())
            return;

        for (var property : this.statesLayoutOfProperties.keySet()) {
            HashMap<String, FlowLayout> statesLayoutOfProperty = this.statesLayoutOfProperties.get(property);
            boolean first = true;
            for (var valueName : statesLayoutOfProperty.keySet()) {
                this.updateStateLayout(property, valueName, first);
                first = false;
            }
        }

        this.updatePreview();
    }

    private void updateStateLayout(Property<?> property, String valueName, boolean isDefault) {
        Optional<BlockState> blockStateOptional = this.blockBuilder.blockState();
        if (blockStateOptional.isEmpty())
            return;
        BlockState blockState = blockStateOptional.get();

        String value = valueName;
        if (isDefault)
            value = blockState.getBlock().getDefaultState().get(property).toString().toLowerCase();

        String propertyName = property.getName();
        FlowLayout stateLayout = this.statesLayoutOfProperties.get(property).get(valueName);
        stateLayout.clearChildren();
        Component blockComponent = Components.block(BlockItem.with(blockState, property, value), this.blockBuilder.nbt())
                .sizing(Sizing.fixed(40), Sizing.fixed(40));
        stateLayout.child(blockComponent);
        boolean propertyUsed = this.blockBuilder.contains(propertyName);
        if ((isDefault && !propertyUsed) || (this.blockBuilder.isState(propertyName, valueName) && propertyUsed && !isDefault)) {
            stateLayout.surface(Surface.flat(0x6050af68));
        } else {
            stateLayout.surface(Surface.flat(60000000))
                    .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    .cursorStyle(CursorStyle.HAND)
                    .sizing(Sizing.fixed(40), Sizing.fixed(40));
            blockComponent.mouseDown().subscribe((mouseX, mouseY, button) -> this.onStateExecute(property, valueName, isDefault));
        }
    }

    private boolean onStateExecute(Property<?> property, String valueName, boolean isDefault) {
        String propertyName = property.getName();

        if (isDefault)
            this.setItem(this.blockBuilder.remove(propertyName).get());
        else
            this.setItem(this.blockBuilder.add(propertyName, valueName).get());

        this.updatePropertiesContent();
        return true;
    }

    private void setTranslation(LabelComponent labelComponent, String key, String value) {
        String translationKey = BLOCK_STATE_TRANSLATION_KEY + key;
        Text translation = Text.translatable(translationKey);

        if (translation.getString().equals(translationKey)) {
            labelComponent.text(Text.of(value));
        } else {
            labelComponent.text(translation);
            labelComponent.tooltip(Text.of(value));
        }
    }
}
