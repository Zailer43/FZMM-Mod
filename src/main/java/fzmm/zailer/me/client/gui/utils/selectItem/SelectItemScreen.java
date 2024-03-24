package fzmm.zailer.me.client.gui.utils.selectItem;

import com.google.common.collect.ImmutableList;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.logic.FzmmHistory;
import fzmm.zailer.me.mixin.PlayerInventoryAccessor;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SelectItemScreen extends BaseFzmmScreen {

    private static final String REQUESTED_ITEMS_LIST_ID = "requested-items-list";
    private static final String INVENTORY_BUTTON_ID = "inventory-button";
    private static final String DEFAULT_BUTTON_ID = "default-button";
    private static final String HISTORY_BUTTON_ID = "history-button";
    private static final String ALL_BUTTON_ID = "all-button";
    private static final String ITEM_LAYOUT_ID = "item-layout";
    private static final String ITEM_SEARCH_ID = "item-search";
    private static final String EXECUTE_BUTTON_ID = "execute";
    private final HashMap<RequestedItem, ItemComponent> requestedItems;
    private final RequestedItem selectedRequestedItem;
    private final List<ItemComponent> itemComponentList;
    private final Consumer<ItemStack> executeCallback;
    private FlowLayout requestedItemsLayout;
    private FlowLayout itemLayout;
    private TextFieldWidget searchField;
    private List<ButtonComponent> sourceButtons;
    private ButtonComponent executeButton;

    public SelectItemScreen(@Nullable Screen parent, RequestedItem requestedItem) {
        this(parent, List.of(requestedItem), (ItemStack -> {}));
    }

    public SelectItemScreen(@Nullable Screen parent, List<RequestedItem> requestedItems, Consumer<ItemStack> executeCallback) {
        super("utils/select_item", "selectItem", parent);
        this.requestedItems = new HashMap<>();
        this.itemComponentList = new ArrayList<>();

        for (var requestedItem : requestedItems) {
            this.requestedItems.put(requestedItem, Components.item(ItemStack.EMPTY));
        }

        this.selectedRequestedItem = requestedItems.get(0);
        this.executeCallback = executeCallback;
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        assert this.client != null;
        assert this.client.player != null;

        // left buttons
        this.requestedItemsLayout = rootComponent.childById(FlowLayout.class, REQUESTED_ITEMS_LIST_ID);
        checkNull(this.requestedItemsLayout, "flow-layout", REQUESTED_ITEMS_LIST_ID);

        // right buttons
        this.itemLayout = rootComponent.childById(FlowLayout.class, ITEM_LAYOUT_ID);
        checkNull(this.itemLayout, "flow-layout", ITEM_LAYOUT_ID);

        this.searchField = TextBoxRow.setup(rootComponent, ITEM_SEARCH_ID, "", 255, str -> this.applyFilter());
        this.searchField.horizontalSizing(Sizing.fill(50));

        this.setupSourceButtons(rootComponent);
        this.addRequestedItemButtons();

        // bottom buttons
        this.executeButton = ButtonRow.setup(rootComponent, ButtonRow.getButtonId(EXECUTE_BUTTON_ID), this.canExecute(), buttonComponent -> {
            for (var requestedItem : this.requestedItems.keySet()) {
                requestedItem.execute();
                requestedItem.updatePreview();
                this.executeCallback.accept(requestedItem.stack());
            }

            this.close();
        });

    }

    private void setupSourceButtons(FlowLayout rootComponent) {
        assert this.client != null;
        assert this.client.player != null;
        ButtonComponent inventoryButton = rootComponent.childById(ButtonComponent.class, INVENTORY_BUTTON_ID);
        checkNull(inventoryButton, "button", INVENTORY_BUTTON_ID);
        inventoryButton.onPress(buttonComponent -> {
            this.sourceButtonsClicked(INVENTORY_BUTTON_ID);

            PlayerInventory inventory = this.client.player.getInventory();
            List<DefaultedList<ItemStack>> inventoryStacks = ((PlayerInventoryAccessor) (inventory)).getCombinedInventory();

            for (var stackList : inventoryStacks)
                this.addItemCallback(stackList, true);
        });

        ButtonComponent defaultButton = rootComponent.childById(ButtonComponent.class, DEFAULT_BUTTON_ID);
        checkNull(defaultButton, "button", DEFAULT_BUTTON_ID);
        defaultButton.onPress(buttonComponent -> {
            this.sourceButtonsClicked(DEFAULT_BUTTON_ID);

            this.addItemCallback(this.selectedRequestedItem.defaultItems(), false);
        });

        ButtonComponent historyButton = rootComponent.childById(ButtonComponent.class, HISTORY_BUTTON_ID);
        checkNull(historyButton, "button", HISTORY_BUTTON_ID);
        historyButton.onPress(buttonComponent -> {
            this.sourceButtonsClicked(HISTORY_BUTTON_ID);

            this.addItemCallback(FzmmHistory.getAllItems(), true);
        });

        ItemGroups.updateDisplayContext(this.client.player.networkHandler.getEnabledFeatures(), true, this.client.player.getWorld().getRegistryManager());
        ButtonComponent allButton = rootComponent.childById(ButtonComponent.class, ALL_BUTTON_ID);
        checkNull(allButton, "button", ALL_BUTTON_ID);
        allButton.onPress(buttonComponent -> {
            this.sourceButtonsClicked(ALL_BUTTON_ID);

            Set<ItemStack> stackList = new LinkedHashSet<>();
            PlayerInventory inventory = this.client.player.getInventory();
            List<DefaultedList<ItemStack>> inventoryStacks = ((PlayerInventoryAccessor) (inventory)).getCombinedInventory();

            for (var list : inventoryStacks)
                stackList.addAll(list);

            for (var itemGroup : ItemGroups.getGroups())
                stackList.addAll(itemGroup.getDisplayStacks());

            this.addItemCallback(stackList, false);
        });


        this.sourceButtons = ImmutableList.of(inventoryButton, defaultButton, historyButton, allButton);

        inventoryButton.onPress();

        if (this.itemComponentList.isEmpty())
            defaultButton.onPress();
    }

    private void addItemCallback(Collection<ItemStack> stackList, boolean filter) {
        List<ItemComponent> itemComponents = new ArrayList<>();
        Predicate<ItemStack> stackPredicate = this.selectedRequestedItem.predicate();
        for (var stack : stackList) {
            if ((!filter || stackPredicate.test(stack)) && !stack.isEmpty())
                itemComponents.add(this.getItemCallback(stack));
        }

        this.itemComponentList.addAll(itemComponents);
        this.applyFilter();
    }

    private ItemComponent getItemCallback(ItemStack stack) {
        assert this.client != null;

        ItemComponent itemComponent = (ItemComponent) Components.item(stack)
                .tooltip(stack.getTooltip(
                        this.client.player,
                        this.client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC
                ));

        itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            this.selectedRequestedItem.setStack(stack);
            this.requestedItems.get(this.selectedRequestedItem).stack(stack);
            this.executeButton.active = this.canExecute();
            return true;
        });

        itemComponent.showOverlay(true);

        return itemComponent;
    }

    private void sourceButtonsClicked(String id) {
        this.itemComponentList.clear();

        for (var sourceButton : this.sourceButtons) {
            sourceButton.active = !id.equals(sourceButton.id());
        }
    }

    private void addRequestedItemButtons() {
        List<RequestedItem> entries = this.requestedItems.keySet().stream().toList();
        List<Component> requestedItemsEntries = new ArrayList<>();
        for (int i = 0; i != entries.size(); i++) {
            RequestedItem entry = entries.get(i);
            requestedItemsEntries.add(this.addRequestedItemButton(entry, entry.stack(), i));
        }

        this.requestedItemsLayout.children(requestedItemsEntries);
    }

    private Component addRequestedItemButton(RequestedItem requestedItem, ItemStack stack, int index) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("id", String.valueOf(index));

        FlowLayout requestedItemLayout = this.getModel().expandTemplate(FlowLayout.class, "requested-item", parameters);
        this.requestedItems.put(requestedItem, this.getRequestedItemPreview(requestedItemLayout, stack, index));

        LabelComponent labelComponent = requestedItemLayout.childById(LabelComponent.class, index + "-requested-item-label");
        if (labelComponent != null) {
            labelComponent.text(requestedItem.title());
        }

        return requestedItemLayout;
    }

    private ItemComponent getRequestedItemPreview(FlowLayout layout, ItemStack stack, int index) {
        ItemComponent itemComponent = layout.childById(ItemComponent.class, index + "-requested-item-item");
        if (itemComponent != null) {
            assert this.client != null;

            itemComponent.stack(stack);
            itemComponent.tooltip(stack.getTooltip(
                    this.client.player,
                    this.client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC
            ));
        }
        return itemComponent;
    }

    private void applyFilter() {
        if (this.searchField == null)
            return;

        this.itemLayout.clearChildren();
        List<Component> resultList = new ArrayList<>();
        String search = this.searchField.getText().toLowerCase();

        for (var itemComponent : this.itemComponentList) {
            if (itemComponent.stack().getName().getString().toLowerCase().contains(search))
                resultList.add(itemComponent);
        }

        this.itemLayout.children(resultList);
    }

    private boolean canExecute() {
        for (var requestedItem : this.requestedItems.keySet()) {
            if (!requestedItem.canExecute()) {
                return false;
            }
        }

        return true;
    }
}
