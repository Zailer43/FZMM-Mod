package fzmm.zailer.me.client.gui.item_editor.base.components;

import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import fzmm.zailer.me.client.gui.utils.selectItem.SelectItemScreen;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class RequestedItemComponent extends FlowLayout implements ICollapsible {
    private static final Text GIVE_ITEM_TEXT = Text.translatable("fzmm.gui.itemEditor.base.label.give");
    private final Consumer<ItemStack> selectedItem;
    private final Runnable updateEditorsComponents;
    private final RequestedItem requestedItem;
    private final ItemComponent stackPreview;
    private final ButtonComponent selectItemButton;
    private final ButtonComponent giveButton;


    public RequestedItemComponent(List<RequestedItem> requestedItemList, RequestedItem requestedItem,
                                  Consumer<ItemStack> selectedItem, Runnable updateEditorsComponents) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);

        this.selectedItem = selectedItem;
        this.updateEditorsComponents = updateEditorsComponents;
        this.requestedItem = requestedItem;

        this.gap(2);
        this.verticalAlignment(VerticalAlignment.CENTER);

        this.stackPreview = Components.item(ItemStack.EMPTY).setTooltipFromStack(true).showOverlay(true);

        MinecraftClient client = MinecraftClient.getInstance();

        this.selectItemButton = Components.button(Text.empty(),
                button -> client.setScreen(new SelectItemScreen(client.currentScreen, List.of(requestedItem),
                        stack -> updateEditorsComponents.run()))
        );
        this.selectItemButton.horizontalSizing(Sizing.fixed(100));

        this.giveButton = Components.button(GIVE_ITEM_TEXT, button -> FzmmUtils.giveItem(requestedItem.stack()));
        this.giveButton.horizontalSizing(Sizing.fixed(30));

        requestedItem.setUpdatePreviewConsumer(itemStack ->
                this.updatePreviewExecute(itemStack, requestedItemList));

        ItemStack stack = requestedItem.stack();
        this.updateRequestedItemButton(selectItemButton, requestedItem, stack);
        this.stackPreview.stack(stack);
    }

    private void updatePreviewExecute(ItemStack itemStack, List<RequestedItem> requestedItemList) {
        boolean firstItem = true;
        for (int i = 0; i != requestedItemList.size(); i++) {
            RequestedItem entry = requestedItemList.get(i);
            if (firstItem && (entry == requestedItem || (requestedItemList.size() - 1) == i)) {
                this.selectedItem.accept(itemStack);
                break;
            } else if (!entry.isEmpty()) {
                firstItem = false;
            }
        }

        this.updateRequestedItem(itemStack);
    }

    public void updateRequestedItem(ItemStack stack) {
        this.stackPreview.stack(stack);
        this.updateRequestedItemButton(this.selectItemButton, this.requestedItem, stack);
        this.updateEditorsComponents.run();
    }

    private void updateRequestedItemButton(ButtonComponent selectItemButton, RequestedItem requestedItem, ItemStack stack) {
        selectItemButton.setMessage(
                requestedItem.predicate().test(stack) ?
                        requestedItem.title() :
                        requestedItem.title().copy().setStyle(Style.EMPTY.withColor(0xD83F27)).append(" ⚠")
        );
    }

    @Override
    public void collapse() {
        this.clearChildren();
        StackLayout layout = Containers.stack(Sizing.content(), Sizing.content());
        ButtonComponent button = Components.button(Text.empty(), buttonComponent -> {
            FlowLayout overlayLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            overlayLayout.padding(Insets.of(6));
            overlayLayout.gap(4);
            overlayLayout.surface(Surface.DARK_PANEL);
            overlayLayout.child(this.selectItemButton);
            overlayLayout.child(this.giveButton);
            overlayLayout.mouseDown().subscribe((mouseX, mouseY, button1) -> true);
            int childX = this.x() + 22;
            int childY = this.y() - 6;

            OverlayContainer<FlowLayout> overlayContainer = new OverlayContainer<>(overlayLayout) {
                @Override
                protected int childMountX() {
                    return childX;
                }

                @Override
                protected int childMountY() {
                    return childY;
                }
            };
            overlayContainer.zIndex(500);
            overlayContainer.surface(Surface.flat(0));

            ParentComponent root = this.root();
            if (root instanceof FlowLayout rootLayout)
                rootLayout.child(overlayContainer);
        });
        button.horizontalSizing(Sizing.fixed(20));
        layout.child(button);
        layout.child(this.stackPreview);
        layout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        this.child(layout);
    }

    @Override
    public void expand() {
        this.clearChildren();
        this.children(List.of(this.stackPreview, this.selectItemButton, this.giveButton));
    }
}
