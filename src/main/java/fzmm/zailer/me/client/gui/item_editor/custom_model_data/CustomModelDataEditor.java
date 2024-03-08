package fzmm.zailer.me.client.gui.item_editor.custom_model_data;

import fzmm.zailer.me.builders.CustomModelDataBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.common.selectable.SelectableEditor;
import fzmm.zailer.me.client.gui.item_editor.custom_model_data.components.CustomModelItemComponent;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import fzmm.zailer.me.mixin.custom_model_data_editor.BakedOverrideAccessor;
import fzmm.zailer.me.mixin.custom_model_data_editor.ModelOverrideListAccessor;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CustomModelDataEditor extends SelectableEditor<CustomModelItemComponent> {
    private RequestedItem stackRequested = null;
    private List<RequestedItem> requestedItems = null;
    private List<Pair<Integer, Item>> customModels;
    private BooleanButton showAllCustomModels;
    private final CustomModelDataBuilder builder = CustomModelDataBuilder.builder();
    private boolean noCustomModels;
    private LabelComponent warningLabel;

    @Override
    public List<RequestedItem> getRequestedItems() {

        if (this.requestedItems != null)
            return this.requestedItems;

        this.stackRequested = new RequestedItem(
                itemStack -> !this.getCustomModels(itemStack).isEmpty(),
                this::selectItemAndUpdateParameters,
                null,
                Text.translatable("fzmm.gui.itemEditor.custom_model_data.item"),
                true
        );

        this.requestedItems = List.of(this.stackRequested);
        return this.requestedItems;
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.GLASS.getDefaultStack();
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        this.noCustomModels = this.stackRequested.defaultItems().isEmpty();
        this.customModels = new ArrayList<>();
        editorLayout = super.getLayout(baseScreen, editorLayout);

        this.showAllCustomModels = editorLayout.childById(BooleanButton.class, "show-all-custom-models");
        BaseFzmmScreen.checkNull(this.showAllCustomModels, "boolean-button", "show-all-custom-models");
        this.showAllCustomModels.onPress(buttonComponent -> this.updateCustomModels());

        this.warningLabel = editorLayout.childById(LabelComponent.class, "warning-label");
        BaseFzmmScreen.checkNull(this.warningLabel, "label", "warning-label");

        return editorLayout;
    }

    @Override
    protected int getMaxByPage() {
        return 100;
    }

    @Override
    protected int getSelectableSize() {
        return this.customModels.size();
    }

    @Override
    protected void select(CustomModelItemComponent component) {
        if (component.stack().isEmpty())
            return;

        this.builder.item(component.stack().getItem()).value(component.getModel().orElse(null));
        this.updateItemPreview();
    }

    @Override
    protected CustomModelItemComponent emptyComponent() {
        return new CustomModelItemComponent();
    }

    @Override
    protected void updateComponent(Component component, int index) {
        if (!(component instanceof CustomModelItemComponent customModelItemComponent))
            return;

        if (index < this.customModels.size()) {
            Pair<Integer, Item> customModel = this.customModels.get(index);
            customModelItemComponent.setModel(customModel.getRight().getDefaultStack(), customModel.getLeft());
        } else {
            customModelItemComponent.clear();
        }
    }

    @Override
    public String getId() {
        return "custom_model_data";
    }

    @Override
    public void updateItemPreview() {
        this.updatePreview();

        this.stackRequested.setStack(this.builder.get());
        this.stackRequested.updatePreview();
    }

    private void updatePreview() {
        this.previewComponent.setModel(this.builder.get(), this.builder.value().orElse(null));
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        this.builder.of(stack);
        this.updateCustomModels();
        this.updatePreview();
    }

    private void setWarningLabel() {
        Text warningText = Text.empty();
        boolean showWarning = false;
        if (this.noCustomModels) {
            warningText = Text.translatable("fzmm.gui.itemEditor.custom_model_data.label.warning.noCustomModels");
            showWarning = true;
            // default item is always added
        } else if (this.customModels.size() <= 1) {
            warningText = Text.translatable("fzmm.gui.itemEditor.custom_model_data.label.warning.itemHasNoCustomModels");
            showWarning = true;
        }

        if (showWarning) {
            this.warningLabel.text(warningText.copy().setStyle(Style.EMPTY.withColor(0xE03F31)));
            this.warningLabel.horizontalSizing(Sizing.expand(100));
            this.warningLabel.margins(Insets.of(8));
        } else {
            this.warningLabel.text(Text.empty());
            this.warningLabel.horizontalSizing(Sizing.content());
            this.warningLabel.margins(Insets.of(0));
        }
    }

    public void updateCustomModels() {
        this.customModels.clear();

        if (this.showAllCustomModels.enabled()) {
            for (var stack : this.stackRequested.defaultItems())
                this.addCustomModelWithDefault(stack);
        } else {
            this.addCustomModelWithDefault(this.builder.get());
        }

        this.setWarningLabel();

        this.currentPage = -1;
        this.setPage(0);
    }

    public void addCustomModelWithDefault(ItemStack stack) {
        this.customModels.add(new Pair<>(null, stack.getItem()));
        this.customModels.addAll(this.getCustomModels(stack));
    }

    private List<Pair<Integer, Item>> getCustomModels(ItemStack itemStack) {
        List<Pair<Integer, Item>> customModels = new ArrayList<>();
        BakedModel bakedModel = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(itemStack.getItem());
        if (bakedModel == null || itemStack.isEmpty())
            return customModels;

        ModelOverrideListAccessor modelOverrideList = (ModelOverrideListAccessor) bakedModel.getOverrides();
        ModelOverrideList.BakedOverride[] bakedOverrides = modelOverrideList.getOverrides();
        Identifier[] conditionTypes = modelOverrideList.getConditionTypes();

        int customModelDataIndex = -1;
        for (int i = 0; i != conditionTypes.length; i++) {
            if (conditionTypes[i].getPath().equals("custom_model_data")) {
                customModelDataIndex = i;
                break;
            }
        }

        if (customModelDataIndex == -1)
            return customModels;

        for (var override : bakedOverrides) {
            ModelOverrideList.InlinedCondition[] conditions = ((BakedOverrideAccessor) override).getConditions();
            for (var condition : conditions) {
                if (condition.index == customModelDataIndex)
                    customModels.add(new Pair<>((int) condition.threshold, itemStack.getItem()));
            }
        }

        customModels.sort(Comparator.comparingInt(Pair::getLeft));

        return customModels;
    }
}
