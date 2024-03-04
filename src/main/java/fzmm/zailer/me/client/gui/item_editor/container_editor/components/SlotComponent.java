package fzmm.zailer.me.client.gui.item_editor.container_editor.components;

import fzmm.zailer.me.client.gui.item_editor.container_editor.ContainerEditor;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SlotComponent extends FlowLayout {
    private final ContainerEditor editor;
    private final ItemComponent item;
    private boolean clicked;
    @Nullable
    private final FlowLayout quickMoveLayout;
    @Nullable
    private final Consumer<SlotComponent> updateStackCallback;
    private final KeyBinding copyKey;
    @Nullable
    private Sprite emptySprite;

    public SlotComponent(ContainerEditor editor, ItemStack stack, @Nullable FlowLayout quickMoveLayout,
                         @Nullable Consumer<SlotComponent> updateStackCallback) {
        super(Sizing.fixed(16), Sizing.fixed(16), Algorithm.VERTICAL);

        this.clicked = false;
        this.editor = editor;
        this.quickMoveLayout = quickMoveLayout;
        this.updateStackCallback = updateStackCallback;
        this.copyKey = MinecraftClient.getInstance().options.pickItemKey;
        this.emptySprite = null;

        this.item = Components.item(stack);
        this.item.setTooltipFromStack(true).showOverlay(true);
        this.surface(Surface.flat(0x20000000));
        this.setupInteractions();

        this.child(this.item);
        this.allowOverflow(true);
    }

    public void setEmptySprite(Identifier emptySprite) {
        this.emptySprite = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(emptySprite);
    }

    private void setupInteractions() {
        this.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (this.copyKey.matchesMouse(button)) {
                this.copyKeyPressed(this.getStack(), this.editor.getSelectedStack());
                return true;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.editor.setLeftClickPressed(true);
                this.clicked = true;

                this.leftClickDown(this.getStack());
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                this.rightClick(this.editor.getSelectedStack(), this.getStack());
                this.editor.setRightClickPressed(true);
            }
            return true;
        });

        this.mouseUp().subscribe((mouseX, mouseY, button) -> {
            if (!this.isInBoundingBox(mouseX, mouseY))
                return false;

            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && this.clicked) {
                this.leftClickUp(this.editor.getSelectedStack(), this.getStack());
                this.clicked = false;
                this.editor.setLeftClickPressed(false);
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                this.editor.setRightClickPressed(false);
            }
            return true;
        });

        // this.item#mouseEnter is used because it opaques to this#mouseEnter of layout
        this.item.mouseEnter().subscribe(() -> {
            this.surface(Surface.flat(0x80FFFFFF));
            this.hovered = true;

            ItemStack selectedStack = this.editor.getSelectedStack();
            ItemStack slotStack = this.getStack();

            if (this.editor.isLeftClickPressed()) {
                this.leftClickDrag(selectedStack, slotStack);
            } else if (this.editor.isRightClickPressed()) {
                this.rightClickDrag(selectedStack, slotStack);
            }
        });

        this.item.mouseLeave().subscribe(() -> {
            this.surface(Surface.flat(0x20000000));
            this.hovered = false;
        });
    }

    // owo-lib doesn't seem to detect the keyPress event, so I use the method
    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (this.hovered && this.copyKey.matchesKey(keyCode, scanCode)) {
            this.copyKeyPressed(this.getStack(), this.editor.getSelectedStack());
            return true;
        }

        return super.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);

        if (this.emptySprite != null && this.getStack().isEmpty()) {
            context.drawSprite(this.x(), this.y(), this.zIndex() + 10, 16, 16, this.emptySprite);
        }
    }

    private void leftClickDown(ItemStack slotStack) {
        if (Screen.hasShiftDown() && !slotStack.isEmpty()) {
            this.insert(slotStack);
            this.clicked = false;
        }
    }

    private void leftClickUp(ItemStack selectedStack, ItemStack slotStack) {
        if (selectedStack.isEmpty() && slotStack.isEmpty())
            return;

        if (ItemStack.canCombine(selectedStack, slotStack))
            this.split(selectedStack, slotStack, selectedStack.getCount());
        else
            this.swap(selectedStack, slotStack);
    }

    private void leftClickDrag(ItemStack selectedStack, ItemStack slotStack) {
        //TODO
    }

    private void rightClick(ItemStack selectedStack, ItemStack slotStack) {
        if (selectedStack.isEmpty() && slotStack.isEmpty())
            return;

        if (selectedStack.isEmpty()) {
            int[] splitCount = this.splitCount(slotStack, selectedStack, (int) Math.ceil(slotStack.getCount() / 2f));

            this.setStack(slotStack.copyWithCount(splitCount[0]));
            this.editor.setSelectedStack(slotStack.copyWithCount(splitCount[1]));
        } else if (ItemStack.canCombine(selectedStack, slotStack) || slotStack.isEmpty() && !selectedStack.isEmpty()) {
            this.split(selectedStack, slotStack, 1);
        } else {
            this.swap(selectedStack, slotStack);
        }
    }

    private void rightClickDrag(ItemStack selectedStack, ItemStack slotStack) {
        if (ItemStack.canCombine(selectedStack, slotStack) || slotStack.isEmpty() && !selectedStack.isEmpty()) {
            this.split(selectedStack, slotStack, 1);
        }
    }

    private void copyKeyPressed(ItemStack slotStack, ItemStack selectedStack) {
        assert MinecraftClient.getInstance().player != null;
        if (!MinecraftClient.getInstance().player.isCreative())
            return;

        if (!slotStack.isEmpty() && selectedStack.isEmpty()) {
            ItemStack copy = slotStack.copyWithCount(this.editor.getMaxStackSize(slotStack));
            this.editor.setSelectedStack(copy);
        } else if (slotStack.isEmpty() && !selectedStack.isEmpty()) {
            ItemStack copy = selectedStack.copyWithCount(this.editor.getMaxStackSize(selectedStack));
            this.setStack(copy);
        }
    }

    private void split(ItemStack selectedStack, ItemStack slotStack, int count) {
        int[] splitCount = this.splitCount(selectedStack, slotStack, count);

        ItemStack combinedStack = (slotStack.isEmpty() ? selectedStack : slotStack).copyWithCount(splitCount[1]);
        selectedStack.setCount(splitCount[0]);
        this.setStack(combinedStack);
        this.editor.setSelectedStack(selectedStack);
    }


    /**
     * @return index 0 = first stack count, index 1 = second stack count
     */
    public int[] splitCount(ItemStack firstStack, ItemStack secondStack, int splitCount) {
        return this.splitCount(firstStack, secondStack, firstStack.getCount(), splitCount);
    }

    /**
     * @return index 0 = first stack count, index 1 = second stack count
     */
    private int[] splitCount(ItemStack firstStack, ItemStack secondStack, int firstStackCount, int splitCount) {
        if (firstStackCount == 0)
            return new int[]{0, secondStack.getCount()};

        ItemStack firstStackCopy = firstStack.copy();
        int maxCount = this.editor.getMaxStackSize(secondStack);
        int secondCount = secondStack.getCount() + firstStackCopy.split(splitCount).getCount();

        firstStackCount -= (secondCount - secondStack.getCount());
        if (secondCount > maxCount) {
            int overflow = secondCount - maxCount;
            secondCount = maxCount;
            firstStackCount += overflow;
        }

        return new int[]{firstStackCount, secondCount};
    }


    private void swap(ItemStack selectedStack, ItemStack slotStack) {
        this.setStack(selectedStack);
        this.editor.setSelectedStack(slotStack);
    }

    private void insert(ItemStack slotStack) {
        if (slotStack.isEmpty() || this.quickMoveLayout == null)
            return;

        AtomicInteger count = new AtomicInteger(slotStack.getCount());
        this.insertPredicate(slotStack, (stackPair) -> ItemStack.canCombine(stackPair.getLeft(), stackPair.getRight()), count);

        if (count.get() > 0)
            this.insertPredicate(slotStack, (stackPair) -> stackPair.getRight().isEmpty(), count);

        if (count.get() != slotStack.getCount())
            this.setStack(slotStack.copyWithCount(count.get()));
    }

    private void insertPredicate(ItemStack stack, Predicate<Pair<ItemStack, ItemStack>> predicate, AtomicInteger count) {
        assert this.quickMoveLayout != null;
        for (var child : this.quickMoveLayout.children()) {
            if (!(child instanceof SlotComponent quickMoveSlotComponent))
                continue;

            if (predicate.test(new Pair<>(stack, quickMoveSlotComponent.getStack())))
                count.set(quickMoveSlotComponent.insert(stack, count.get()));

            if (count.get() < 1)
                return;
        }
    }

    /**
     * @return quick move count
     */
    private int insert(ItemStack quickMoveStack, int quickMoveCount) {
        ItemStack slotStack = this.getStack();
        int[] splitCount = this.splitCount(quickMoveStack, slotStack, quickMoveCount, quickMoveCount);

        this.setStack(quickMoveStack.copyWithCount(splitCount[1]));

        return splitCount[0];
    }

    public void setStack(ItemStack stack) {
        this.item.stack(stack.isEmpty() ? ItemStack.EMPTY : stack);

        if (this.updateStackCallback != null)
            this.updateStackCallback.accept(this);
    }

    public ItemStack getStack() {
        return this.item.stack();
    }
}
