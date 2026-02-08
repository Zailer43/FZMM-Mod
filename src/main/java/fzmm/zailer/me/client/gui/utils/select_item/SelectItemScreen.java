package fzmm.zailer.me.client.gui.utils.select_item;

import com.google.common.collect.ImmutableList;
import fzmm.zailer.me.client.FzmmItemGroup;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.logic.history.FzmmHistory;
import fzmm.zailer.me.utils.InventoryUtils;
import fzmm.zailer.me.utils.ItemUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.FocusHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class SelectItemScreen extends BaseFzmmScreen {
    private final HashMap<RequestedItem, ItemComponent> requestedItems;
    private final RequestedItem selectedRequestedItem;
    private final List<ItemComponent> itemComponentList;
    private FlowLayout requestedItemsLayout;
    private FlowLayout itemLayout;
    private EditBox searchField;
    private List<ButtonComponent> sourceButtons;
    private ButtonComponent executeButton;
    private boolean executed = false;

    public SelectItemScreen(@Nullable Screen parent, RequestedItem requestedItem) {
        this(parent, List.of(requestedItem));
    }

    public SelectItemScreen(@Nullable Screen parent, List<RequestedItem> requestedItems) {
        super("utils/select_item", "selectItem", parent);
        this.requestedItems = new HashMap<>();
        this.itemComponentList = new ArrayList<>();

        for (var requestedItem : requestedItems) {
            this.requestedItems.put(requestedItem, EComponents.item(ItemStack.EMPTY));
        }

        this.selectedRequestedItem = requestedItems.get(0);
    }

    @Override
    protected void setup(EFlowLayout rootComponent) {
        assert this.minecraft.player != null;

        // left buttons
        this.requestedItemsLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "requested-items-list");

        // right buttons
        this.itemLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "item-layout");

        this.searchField = TextBoxRow.setup(rootComponent, "item-search", "", 255, str -> this.applyFilter());
        this.searchField.horizontalSizing(Sizing.fill(50));

        this.setupSourceButtons(rootComponent);
        this.addRequestedItemButtons();

        // bottom buttons
        this.executeButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "execute-button");
        this.executeButton.active(this.canExecute());
        this.executeButton.onPress(buttonComponent -> {
            this.execute(false);
            this.onClose();
        });
    }

    @Override
    protected void initFocus(FocusHandler focusHandler) {
        focusHandler.focus(this.searchField, UIComponent.FocusSource.MOUSE_CLICK);
    }

    private void setupSourceButtons(EFlowLayout rootComponent) {
        assert this.minecraft.player != null;
        EButtonComponent inventoryButton = rootComponent.childByIdOrThrow(EButtonComponent.class, "inventory-button");
        inventoryButton.onPress(buttonComponent -> {
            this.sourceButtonsClicked(inventoryButton.id());

            this.addItemCallback(InventoryUtils.getCombinedInventory(), true);
        });

        EButtonComponent defaultButton = rootComponent.childByIdOrThrow(EButtonComponent.class, "default-button");
        defaultButton.onPress(buttonComponent -> {
            this.sourceButtonsClicked(defaultButton.id());

            this.addItemCallback(this.selectedRequestedItem.defaultItems(), false);
        });

        EButtonComponent historyButton = rootComponent.childByIdOrThrow(EButtonComponent.class, "history-button");
        historyButton.onPress(buttonComponent -> {
            this.sourceButtonsClicked(historyButton.id());

            this.addItemCallback(FzmmHistory.getAllItems(), true);
        });

        FzmmItemGroup.populateItemGroups();
        EButtonComponent allButton = rootComponent.childByIdOrThrow(EButtonComponent.class, "all-button");
        allButton.onPress(buttonComponent -> {
            this.sourceButtonsClicked(allButton.id());

            Set<ItemStack> stackList = new LinkedHashSet<>(InventoryUtils.getCombinedInventory());

            for (var itemGroup : CreativeModeTabs.allTabs())
                stackList.addAll(itemGroup.getDisplayItems());

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
        assert this.minecraft != null;

        ItemStack processedStack = ItemUtils.process(stack);

        ItemComponent itemComponent = EComponents.item(processedStack).setTooltipFromStack(true);

        itemComponent.mouseDown().subscribe((input, doubled) -> {
            this.selectedRequestedItem.setStack(processedStack);
            this.requestedItems.get(this.selectedRequestedItem).stack(processedStack);
            this.executeButton.active = this.canExecute();
            return true;
        });

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
        List<UIComponent> requestedItemsEntries = new ArrayList<>();
        for (int i = 0; i != entries.size(); i++) {
            RequestedItem entry = entries.get(i);
            requestedItemsEntries.add(this.addRequestedItemButton(entry, entry.stack().orElse(ItemStack.EMPTY), i));
        }

        this.requestedItemsLayout.children(requestedItemsEntries);
    }

    private UIComponent addRequestedItemButton(RequestedItem requestedItem, ItemStack stack, int index) {
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
            itemComponent.stack(stack);
            itemComponent.tooltip(stack.getTooltipLines(
                    Item.TooltipContext.EMPTY,
                    this.minecraft.player,
                    this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL
            ));
        }
        return itemComponent;
    }

    private void applyFilter() {
        if (this.searchField == null)
            return;

        this.itemLayout.clearChildren();
        List<UIComponent> resultList = new ArrayList<>();
        String search = this.searchField.getValue().toLowerCase();

        for (var itemComponent : this.itemComponentList) {
            if (itemComponent.stack().getHoverName().getString().toLowerCase().contains(search))
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

    private void execute(boolean replaceWithEmpty) {
        this.executed = true;
        for (var requestedItem : this.requestedItems.keySet()) {
            if (!replaceWithEmpty && requestedItem.canExecute()) {
                requestedItem.execute();
            } else {
                requestedItem.execute(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void removed() {
        super.removed();
        if (!this.executed) {
            this.execute(true);
        }
    }
}
