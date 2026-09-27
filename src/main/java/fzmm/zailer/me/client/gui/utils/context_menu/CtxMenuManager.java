package fzmm.zailer.me.client.gui.utils.context_menu;

import com.mojang.blaze3d.platform.InputConstants;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.FzmmHotkeys;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.utils.DeferredMountComponent;
import fzmm.zailer.me.mixin.component.context_menu.AbstractContainerScreenAccessor;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.layers.Layer;
import io.wispforest.owo.ui.layers.Layers;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CtxMenuManager<T> {
    public static final int NESTED_OVERLAP = 3;
    protected final CtxNode<T> node;
    protected final Function<List<UIComponent>, T> mapper;
    protected StackLayout layout = null;
    @Nullable
    protected Consumer<ParentUIComponent> configureNode = null;

    public CtxMenuManager(List<CtxElement<T>> entries, Function<List<UIComponent>, T> mapper) {
        this.node = CtxNode.of("base", entries);
        this.mapper = mapper;
    }

    public static List<ItemStack> parseItems(List<UIComponent> components) {
        var result = new ArrayList<ItemStack>(components.size());
        for (var component : components) {
            result.add((component instanceof ItemComponent itemComponent ? itemComponent.stack() : ItemStack.EMPTY).copy());
        }
        return result;
    }

    public static void registerUILayers() {
        Layers.add((horizontal, vertical) -> (FlowLayout) EContainers.verticalFlow(Sizing.content(), Sizing.content()),
                instance -> {
                    ScreenKeyboardEvents.allowKeyPress(instance.screen).register((screen, event) -> {
                        if (!FzmmHotkeys.contextMenuKey().matches(event)) return true;

                        executeUILayer(screen, instance);
                        return false;
                    });

                    ScreenMouseEvents.allowMouseClick(instance.screen).register((screen, event) -> {
                        if (!FzmmClient.CONFIG.general.openContextMenuWithAltRightClick()) return true;
                        if (!event.hasAltDown() || event.button() != InputConstants.MOUSE_BUTTON_RIGHT) return true;

                        executeUILayer(screen, instance);
                        return false;
                    });
                }, CreativeModeInventoryScreen.class, InventoryScreen.class
        );
    }

    private static void executeUILayer(Screen screen, Layer<? extends Screen, FlowLayout>.Instance instance) {
        var hoveredSlot = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        instance.adapter.rootComponent.clearChildren();
        new CtxMenuManager<>(CtxMenuActions.itemActions(), CtxMenuManager::parseItems)
                .initInInventory(hoveredSlot.getItem(), screen);
    }

    public void initInInventory(ItemStack focusedStack, Screen screen) {
        var layers = Layers.getInstances(screen);
        if (layers.isEmpty() || !(layers.get(0).adapter.rootComponent instanceof FlowLayout root)) return;

        this.createLayout(root, true);
        this.init(List.of(EComponents.item(focusedStack)), CtxType.INVENTORY, true);
    }

    public boolean init(List<UIComponent> componentSelection, @Nullable Screen screen, boolean focus) {
        this.clearChildren();
        if (!(screen instanceof BaseFzmmScreen baseScreen)) return false;

        this.createLayout(baseScreen.root(), false);
        this.init(componentSelection, CtxType.UI, focus);

        return true;
    }

    protected void init(List<UIComponent> componentSelection, CtxType type, boolean focus) {
        this.layout.child(new DeferredMountComponent((parent, mouseX, mouseY) -> {
            this.initAt(componentSelection, type, mouseX, mouseY);
            if (focus) {
                this.focus();
            }
        }));
    }

    public void initAt(List<UIComponent> componentSelection, CtxType type, int x, int y) {
        this.child(this.createRootNode(componentSelection, type), null, x, y);
    }

    public ParentUIComponent createRootNode(List<UIComponent> componentSelection, CtxType type) {
        var result = this.node.createNode(this, this.mapper.apply(componentSelection), type);
        if (this.configureNode != null) {
            this.configureNode.accept(result);
        }
        return result;
    }

    public CtxNode<T> rootNode() {
        return this.node;
    }

    public CtxMenuManager<T> createLayout(FlowLayout root, boolean isLayer) {
        this.layout = UIContainers.stack(Sizing.fill(100), Sizing.fill(100));
        this.layout.mouseDown().subscribe((click, doubled) -> {
            if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) return this.clearChildren();
            return true;
        });

        if (!isLayer) {
            this.layout.positioning(Positioning.absolute(0, 0));
        }
        root.child(this.layout);
        return this;
    }

    public void focus() {
        var children = this.layout.children();
        if (children.isEmpty() || !(children.get(children.size() - 1) instanceof ParentUIComponent nodeLayout)) return;

        var layoutOptional = this.node.findLayout(nodeLayout);
        if (layoutOptional.isEmpty()) return;

        var focusHandler = layoutOptional.get().focusHandler();
        if (layoutOptional.get().children().isEmpty() || focusHandler == null) return;

        focusHandler.focus(layoutOptional.get().children().get(0), UIComponent.FocusSource.KEYBOARD_CYCLE);
    }

    public CtxMenuManager<T> configureNode(Consumer<ParentUIComponent> value) {
        this.configureNode = value;
        return this;
    }

    protected void child(UIComponent menu, @Nullable UIComponent nested, int x, int y) {
        var child = menu;
        if (nested != null) {
            child = nested;
        }
        this.layout.child(child); // width/height require a mounted (or inflated) component
        if (nested != null) {
            x = this.nestedPositioningX(this.layout.width(), menu.x(), menu.width(), nested.width());
            y = this.lastMenuPositioningY(this.layout.height(), y, nested.height());
        }
        int padding = 5;
        x = Math.clamp(x, padding, Math.abs(this.layout.width() - child.width() - padding));
        y = Math.clamp(y, padding, Math.abs(this.layout.height() - child.height() - padding));

        child.margins(Insets.left(x).withTop(y)); // It doesn’t use absolute positioning because, due to a bug, it doesn’t render in Layers
    }

    private int nestedPositioningX(int maxWidth, int parentX, int parentWidth, int nestedWidth) {
        maxWidth -= 4;
        int rightX = parentX + parentWidth - NESTED_OVERLAP; // It looks slightly better visually if it overlaps the parent a little
        if (rightX + nestedWidth <= maxWidth) return rightX - 1;

        return Math.max(4, parentX - nestedWidth + 1 + NESTED_OVERLAP);
    }

    private int lastMenuPositioningY(int maxHeight, int desiredY, int nestedHeight) {
        maxHeight -= 4;
        if (desiredY + nestedHeight <= maxHeight) return desiredY - 1;

        return Math.max(4, maxHeight - nestedHeight + 1);
    }

    public boolean isUnmounted() {
        return this.layout == null || this.layout.parent() == null;
    }

    public boolean clearChildren() {
        if (this.isUnmounted()) return false;
        assert this.layout.parent() != null;

        this.layout.parent().removeChild(this.layout);
        this.layout.clearChildren();
        this.layout = null;
        return true;
    }

    protected void clearMenusAfter(ParentUIComponent menuLayout) {
        if (this.layout == null) return;
        var children = this.layout.children();

        for (int i = children.size() - 1; i >= 1; i--) {
            var child = children.get(i);
            if (child == menuLayout) return;

            this.layout.removeChild(child);
        }
    }

}
