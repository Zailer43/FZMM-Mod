package fzmm.zailer.me.client.gui.item_editor.container_editor;

import fzmm.zailer.me.builders.ContainerBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.container_editor.components.SlotComponent;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.InventoryUtils;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ContainerEditor implements IItemEditorScreen {
    private RequestedItem containerRequested = null;
    private List<RequestedItem> requestedItems = null;
    private FlowLayout containerInventoryLayout;
    private FlowLayout playerInventoryLayout;
    private FlowLayout playerArmorLayout;
    private FlowLayout playerOffhandLayout;
    private FlowLayout playerContainerLayout;
    private int containerMaxSize;
    private final ContainerBuilder builder = ContainerBuilder.builder();
    private ItemStack selectedStack;
    private BooleanButton vanillaMaxCountButton;
    private boolean ignoreItemPreviewUpdate;
    private boolean updatePlayerInventory;
    private boolean leftClickPressed;
    private boolean rightClickPressed;

    @Override
    public List<RequestedItem> getRequestedItems() {
        if (this.requestedItems != null)
            return this.requestedItems;

        this.containerRequested = new RequestedItem(
                itemStack -> InventoryUtils.getContainerSize(itemStack.getItem()) > 0,
                this::selectItemAndUpdateParameters,
                null,
                Text.translatable("fzmm.gui.itemEditor.container.title"),
                true
        );

        this.requestedItems = List.of(this.containerRequested);
        return this.requestedItems;
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.CHEST.getDefaultStack();
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        assert MinecraftClient.getInstance().player != null;

        this.setSelectedStack(ItemStack.EMPTY);
        this.ignoreItemPreviewUpdate = false;
        this.updatePlayerInventory = MinecraftClient.getInstance().player.isCreative();
        this.leftClickPressed = false;
        this.rightClickPressed = false;

        // inventory
        this.containerInventoryLayout = editorLayout.childById(FlowLayout.class, "container-inventory");
        BaseFzmmScreen.checkNull(this.containerInventoryLayout, "flow-layout", "container-inventory");

        this.playerContainerLayout = editorLayout.childById(FlowLayout.class, "player-container");
        BaseFzmmScreen.checkNull(this.playerContainerLayout, "flow-layout", "player-container");

        this.playerInventoryLayout = editorLayout.childById(FlowLayout.class, "player-inventory");
        BaseFzmmScreen.checkNull(this.playerInventoryLayout, "flow-layout", "player-inventory");

        this.playerArmorLayout = editorLayout.childById(FlowLayout.class, "player-armor");
        BaseFzmmScreen.checkNull(this.playerArmorLayout, "flow-layout", "player-armor");

        this.playerOffhandLayout = editorLayout.childById(FlowLayout.class, "player-offhand");
        BaseFzmmScreen.checkNull(this.playerOffhandLayout, "flow-layout", "player-offhand");

        //options
        this.vanillaMaxCountButton = editorLayout.childById(BooleanButton.class, "vanilla-max-count");
        this.vanillaMaxCountButton.enabled(true);

        ButtonComponent sortContainerButton = editorLayout.childById(ButtonComponent.class, "sort-container");
        BaseFzmmScreen.checkNull(sortContainerButton, "button", "sort-container");
        sortContainerButton.onPress(buttonComponent -> this.sortContainer());

        ButtonComponent incrementContainerSizeButton = editorLayout.childById(ButtonComponent.class, "increment-container-size");
        BaseFzmmScreen.checkNull(incrementContainerSizeButton, "button", "increment-container-size");
        incrementContainerSizeButton.onPress(buttonComponent -> this.incrementContainerSize());

        ButtonComponent removeDuplicatesButton = editorLayout.childById(ButtonComponent.class, "remove-duplicates");
        BaseFzmmScreen.checkNull(removeDuplicatesButton, "button", "remove-duplicates");
        removeDuplicatesButton.onPress(buttonComponent -> this.removeDuplicates());

        BoxComponent deleteItemBox = editorLayout.childById(BoxComponent.class, "delete-item");
        BaseFzmmScreen.checkNull(deleteItemBox, "box", "delete-item");
        deleteItemBox.mouseDown().subscribe((mouseX, mouseY, button) -> this.deleteItem());

        return editorLayout;
    }

    @Override
    public String getId() {
        return "container";
    }

    @Override
    public void updateItemPreview() {
        // avoid unnecessary updates in loops
        if (this.ignoreItemPreviewUpdate)
            return;
        this.containerRequested.setStack(this.builder.get().copy());
        this.containerRequested.updatePreview();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        this.builder.of(stack, true);
        this.containerMaxSize = this.builder.containerMaxSize();

        this.buildContainerSlots();
        this.buildPlayerSlots();
        this.updateContainerLayoutSize();
        this.updatePlayerLayoutSize();
        this.setSelectedStack(ItemStack.EMPTY);
    }

    private void buildContainerSlots() {
        List<ItemStack> stackList = this.builder.items();
        this.buildSlots(this.containerInventoryLayout, stackList, this.containerMaxSize, this.playerInventoryLayout,
                this::containerUpdateStackCallback);
    }

    private void buildPlayerSlots() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        PlayerInventory inventory = client.player.getInventory();

        int hotbarSize = PlayerInventory.getHotbarSize();
        List<ItemStack> stackList = new ArrayList<>(inventory.main.subList(hotbarSize, inventory.main.size()));
        stackList.addAll(inventory.main.subList(0, hotbarSize));
        int craftSlotsSize = 5;
        int inventoryStart = craftSlotsSize + inventory.armor.size();
        int offhandStart = inventoryStart + inventory.main.size();

        this.buildSlots(this.playerInventoryLayout, stackList, stackList.size(), this.containerInventoryLayout, (slotComponent, index) -> {
            if (this.updatePlayerInventory)
                // craft slots
                FzmmUtils.updateSlot(inventoryStart + index, slotComponent.getStack());
        });

        this.buildSlots(this.playerArmorLayout, inventory.armor, inventory.armor.size(), this.containerInventoryLayout, (slotComponent, index) -> {
            if (this.updatePlayerInventory)
                FzmmUtils.updateSlot(craftSlotsSize + index, slotComponent.getStack());
        });
        this.setSlotsEmptyIcon(this.playerArmorLayout, List.of(
                PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE,
                PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE,
                PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE,
                PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE
        ));

        this.buildSlots(this.playerOffhandLayout, inventory.offHand, inventory.offHand.size(), this.containerInventoryLayout, (slotComponent, index) -> {
            if (this.updatePlayerInventory)
                FzmmUtils.updateSlot(offhandStart + index, slotComponent.getStack());
        });
        this.setSlotsEmptyIcon(this.playerOffhandLayout, List.of(PlayerScreenHandler.EMPTY_OFFHAND_ARMOR_SLOT));
    }

    private void buildSlots(FlowLayout layout, List<ItemStack> stackList, int containerMaxSize,
                            @Nullable FlowLayout quickMoveLayout, SlotUpdateCallback updateStackCallback) {
        layout.clearChildren();
        List<SlotComponent> slotComponents = new ArrayList<>();

        int size = Math.min(containerMaxSize, stackList.size());

        for (int i = 0; i < size; i++) {
            int index = i;
            slotComponents.add(new SlotComponent(this, stackList.get(i), quickMoveLayout,
                    slot -> updateStackCallback.onSlotUpdate(slot, index)));
        }

        layout.children(slotComponents);
        layout.allowOverflow(true);
    }

    private void addSlot(FlowLayout layout, @Nullable FlowLayout quickMoveLayout, SlotUpdateCallback updateStackCallback) {
        int index = layout.children().size();
        layout.child(new SlotComponent(this, ItemStack.EMPTY, quickMoveLayout,
                        slot -> updateStackCallback.onSlotUpdate(slot, index))
        );
    }

    private void setSlotsEmptyIcon(FlowLayout layout, List<Identifier> slotIcons) {
        List<Component> children = layout.children();
        int size = Math.min(children.size(), slotIcons.size());
        for (int i = 0; i < size; i++) {
            if (children.get(i) instanceof SlotComponent slotComponent)
                slotComponent.setEmptySprite(slotIcons.get(i));
        }
    }

    private void updateContainerLayoutSize() {
        int slotsByColumn = this.getSlotsByColumn();
        int padding = this.containerInventoryLayout.padding().get().left();
        int paddingSize = padding * 2;
        int slotSize = 16;

        int containerRows = (int) Math.ceil(this.containerMaxSize / (float) slotsByColumn);

        int horizontalSize = slotsByColumn * slotSize + paddingSize;
        int containerVerticalSize = containerRows * slotSize + paddingSize;

        this.containerInventoryLayout.sizing(Sizing.fixed(horizontalSize), Sizing.fixed(containerVerticalSize));
        this.containerInventoryLayout.padding(Insets.of(padding));

    }

    private void updatePlayerLayoutSize() {
        int slotsByColumn = this.getSlotsByColumn();
        int padding = this.playerInventoryLayout.padding().get().left();
        int paddingSize = padding * 2;
        int slotSize = 16;

        int playerRows = (int) Math.ceil(this.playerInventoryLayout.children().size() / (float) slotsByColumn);

        int horizontalSize = slotsByColumn * slotSize + paddingSize;
        int playerVerticalSize = playerRows * slotSize + paddingSize;

        this.playerContainerLayout.horizontalSizing(Sizing.fixed(horizontalSize));
        this.playerInventoryLayout.sizing(Sizing.fixed(horizontalSize), Sizing.fixed(playerVerticalSize));
        this.playerInventoryLayout.padding(Insets.of(padding));
    }

    private int getSlotsByColumn() {
        return 9;
    }

    private void sortContainer() {
        this.ignoreItemPreviewUpdate = true;
        boolean isVanillaMaxCount = true;

        List<ItemStack> stackList = this.builder.items();
        stackList.removeIf(ItemStack::isEmpty);
        List<Pair<ItemStack, Integer>> pairList = this.getStackListCount(stackList);

        for (var pair : pairList) {
            ItemStack stack = pair.getLeft();
            if (stack.getCount() > stack.getMaxCount()) {
                isVanillaMaxCount = false;
                break;
            }
        }

        // enable or disable the maximum vanilla stack to prevent them from overflow the container
        // in case they have stacks of 127 but the option disabled, making them not fit in the container
        this.vanillaMaxCountButton.enabled(isVanillaMaxCount);

        int index = 0;
        for (var component : this.containerInventoryLayout.children()) {
            if (!(component instanceof SlotComponent slotComponent)) {
                continue;
            }

            if (index >= pairList.size()) {
                slotComponent.setStack(ItemStack.EMPTY);
                continue;
            }

            Pair<ItemStack, Integer> pair = pairList.get(index);
            ItemStack stack = pair.getLeft();
            int count = pair.getRight();

            ItemStack stackCopy = stack.copyWithCount(this.getMaxStackSize(stack));

            int[] splitCount = slotComponent.splitCount(stackCopy, ItemStack.EMPTY, count);
            count -= splitCount[1];
            pair.setRight(count);

            if (count < 1)
                index++;

            slotComponent.setStack(stack.copyWithCount(splitCount[1]));
        }

        this.ignoreItemPreviewUpdate = false;
        this.updateItemPreview();
    }

    private void incrementContainerSize() {
        // slots are stored in one byte
        if (this.containerMaxSize > Byte.MAX_VALUE)
            return;

        this.containerMaxSize = this.builder.incrementContainerSize();
        this.addSlot(this.containerInventoryLayout, this.playerInventoryLayout, this::containerUpdateStackCallback);
        this.updateContainerLayoutSize();

    }

    private void containerUpdateStackCallback(SlotComponent slotComponent, int index) {
        this.builder.set(index, slotComponent.getStack());
        this.updateItemPreview();
    }

    private List<Pair<ItemStack, Integer>> getStackListCount(List<ItemStack> stackList) {
        List<Pair<ItemStack, Integer>> stackPairList = new ArrayList<>();

        for (var pair : stackList) {
            ItemStack stack = this.getFromList(pair, stackPairList);
            boolean isInList = false;
            for (var stackPair : stackPairList) {
                if (ItemStack.canCombine(stackPair.getLeft(), stack)) {
                    stackPair.setRight(stackPair.getRight() + pair.getCount());

                    isInList = true;
                    break;
                }
            }

            if (!isInList)
                stackPairList.add(new Pair<>(stack, pair.getCount()));
        }

        return stackPairList;
    }

    private ItemStack getFromList(ItemStack stack, List<Pair<ItemStack, Integer>> pairList) {
        for (var pair : pairList) {
            if (ItemStack.canCombine(stack, pair.getLeft())) {
                return pair.getLeft();
            }
        }
        return stack;
    }

    public int getMaxStackSize(ItemStack stack) {
        return this.isVanillaMaxCount() ? stack.getMaxCount() : Byte.MAX_VALUE;
    }

    private void removeDuplicates() {
        boolean hasDuplicates = false;
        this.ignoreItemPreviewUpdate = true;

        List<Component> components = this.containerInventoryLayout.children();

        // is run backwards so that the ones to be deleted are the last ones
        for (int i = components.size() - 1; i >= 0; i--) {
            if (components.get(i) instanceof SlotComponent slotComponent) {
                if (!slotComponent.getStack().isEmpty() && this.builder.contains(slotComponent.getStack())) {
                    hasDuplicates = true;
                    slotComponent.setStack(ItemStack.EMPTY);
                }
            }
        }

        this.ignoreItemPreviewUpdate = false;
        if (hasDuplicates) {
            MinecraftClient.getInstance().getSoundManager()
                    .play(PositionedSoundInstance.master(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 1));

            this.updateItemPreview();
        }
    }

    private boolean deleteItem() {
        if (!this.getSelectedStack().isEmpty()) {
            MinecraftClient.getInstance().getSoundManager()
                    .play(PositionedSoundInstance.master(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 1));
            this.setSelectedStack(ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    public void setSelectedStack(ItemStack stack) {
        this.selectedStack = stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    public ItemStack getSelectedStack() {
        return this.selectedStack;
    }

    public boolean isVanillaMaxCount() {
        return this.vanillaMaxCountButton.enabled();
    }

    public void setLeftClickPressed(boolean leftClickPressed) {
        this.leftClickPressed = leftClickPressed;
        this.rightClickPressed = false;
    }

    public boolean isLeftClickPressed() {
        return this.leftClickPressed;
    }

    public void setRightClickPressed(boolean rightClickPressed) {
        this.rightClickPressed = rightClickPressed;
        this.leftClickPressed = false;
    }

    public boolean isRightClickPressed() {
        return this.rightClickPressed;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.fixMouseUp(mouseX, mouseY, button, this.containerInventoryLayout) ||
                this.fixMouseUp(mouseX, mouseY, button, this.playerInventoryLayout) ||
                this.fixMouseUp(mouseX, mouseY, button, this.playerArmorLayout) ||
                this.fixMouseUp(mouseX, mouseY, button, this.playerOffhandLayout);
    }

    private boolean fixMouseUp(double mouseX, double mouseY, int button, FlowLayout layout) {
        for (var children : layout.children()) {
            if (children.onMouseUp(mouseX, mouseY, button))
                return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.fixKeyPressed(keyCode, scanCode, modifiers, this.containerInventoryLayout) ||
                this.fixKeyPressed(keyCode, scanCode, modifiers, this.playerInventoryLayout) ||
                this.fixKeyPressed(keyCode, scanCode, modifiers, this.playerArmorLayout) ||
                this.fixKeyPressed(keyCode, scanCode, modifiers, this.playerOffhandLayout);
    }

    private boolean fixKeyPressed(int keyCode, int scanCode, int modifiers, FlowLayout layout) {
        for (var children : layout.children()) {
            if (children.onKeyPress(keyCode, scanCode, modifiers))
                return true;
        }
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.selectedStack != null) {
            context.drawItemWithoutEntity(this.selectedStack, mouseX - 8, mouseY - 8);
            context.drawItemInSlot(MinecraftClient.getInstance().textRenderer, this.selectedStack, mouseX - 8, mouseY - 8);
        }
    }

    private interface SlotUpdateCallback {
        void onSlotUpdate(SlotComponent slotComponent, int index);
    }
}
