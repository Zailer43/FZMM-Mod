package fzmm.zailer.me.client.gui.components.containers;

import com.mojang.blaze3d.platform.InputConstants;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.FzmmHotkeys;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.utils.context_menu.CtxMenuManager;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.parsing.UIParsing;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class SelectableLayout extends EFlowLayout {
    protected static final Surface SELECTED_SURFACE;
    @Nullable
    protected CtxMenuManager<?> contextMenu = null;
    protected Surface selectionSurface = SELECTED_SURFACE;
    protected List<Integer> selected = new ObjectArrayList<>();
    protected int lastSelected = -1;
    protected int hoverIndex = -1;
    private boolean wasKeyPressed = false;

    public SelectableLayout(Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm) {
        super(horizontalSizing, verticalSizing, algorithm);
        this.keyPress().subscribe(input -> {
            this.wasKeyPressed = FzmmHotkeys.contextMenuKey().matches(input) && this.openContextMenu();
            return this.wasKeyPressed;
        });

        this.charTyped().subscribe(input -> {
            boolean result = this.wasKeyPressed; // Hack: cancel char event if hotkey was triggered from key event
            this.wasKeyPressed = false;
            return result;
        });
    }

    public <T> void contextMenu(CtxMenuManager<T> value) {
        this.contextMenu = value;
    }

    public int indexOf(double x, double y) {
        var child = this.childAt((int) x + this.x, (int) y + this.y);
        if (child == null) return -1;

        // child could itself have a child, look for the direct child
        while (child.parent() != null && child.parent() != this) {
            child = child.parent();
        }

        return this.children().indexOf(child);
    }

    public Optional<UIComponent> fromIndex(int index) {
        var children = this.children();
        if (index < 0 || index >= children.size()) return Optional.empty();
        return Optional.of(children.get(index));
    }

    @Override
    public void onChildMutated(UIComponent child) {
        super.onChildMutated(child);
        this.clearSelection();
    }

    @Override
    protected void updateLayout() {
        super.updateLayout();
        this.clearSelection();
    }

    public List<UIComponent> selected() {
        var result = new ObjectArrayList<UIComponent>();
        var children = this.children();
        var iterator = this.selected.iterator();

        while (iterator.hasNext()) {
            int index = iterator.next();
            if (index < 0 || index >= children.size()) {
                iterator.remove();
                continue;
            }

            // avoid unmounted components
            var child = children.get(index);
            if (child.parent() != this) {
                iterator.remove();
                continue;
            }

            result.add(child);
        }

        return result;
    }

    protected boolean isSelected(int index) {
        if (index == -1) return false;
        if (this.lastSelected == index) return true;

        return this.selected.contains(index);
    }

    protected void select(int index) {
        this.lastSelected = index;
        this.selected.add(index);
    }

    protected void unselect(int index) {
        this.lastSelected = -1;
        this.selected.remove((Object) index);
    }

    public void clearSelection() {
        this.lastSelected = -1;
        this.selected.clear();
    }

    public void selectionSurface(Surface value) {
        this.selectionSurface = value;
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        double x = click.x();
        double y = click.y();
        if (click.hasShiftDown()) return this.shiftClick(x, y);
        if (click.hasControlDown()) return this.ctrlClick(x, y);
        if (FzmmClient.CONFIG.general.openContextMenuWithAltRightClick() && click.hasAltDown() && click.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            return this.openContextMenu();
        }

        this.clearSelection();
        return super.onMouseDown(click, doubled);
    }

    /**
     * Selects a range of entries
     */
    public boolean shiftClick(double x, double y) {
        int index = this.indexOf(x, y);
        if (index == -1) return false;

        if (this.lastSelected == -1) {
            return this.ctrlClick(x, y);
        } else if (this.lastSelected != index) {
            int[] range = IntStream.rangeClosed(Math.min(this.lastSelected, index), Math.max(this.lastSelected, index)).toArray();
            this.lastSelected = index;
            for (var i : range) {
                if (this.selected.contains(i)) continue;
                this.selected.add(i);
            }
        }

        return true;
    }

    /**
     * Selects or deselects an entry
     */
    public boolean ctrlClick(double x, double y) {
        int index = this.indexOf(x, y);
        if (index == -1) return false;

        if (this.isSelected(index)) {
            this.unselect(index);
        } else {
            this.select(index);
        }

        return true;
    }

    /**
     * Select under cursor if missing and use selected entries in context menu
     */
    private boolean openContextMenu() {
        if (this.contextMenu == null) return false;
        var selected = this.selected();

        this.fromIndex(this.hoverIndex).ifPresent(component -> {
            if (!selected.contains(component)) {
                selected.add(component);
            }
        });
        if (selected.isEmpty()) return false;

        return this.contextMenu.init(selected, Minecraft.getInstance().gui.screen(), true);
    }

    @Override
    protected void drawChildren(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta, List<? extends UIComponent> children) {
        this.selectionSurface.draw(graphics, this);
        super.drawChildren(graphics, mouseX, mouseY, partialTicks, delta, children);
        this.hoverIndex = this.indexOf(mouseX - this.x(), mouseY - this.y());
    }

    public static SelectableLayout parse(Element element) {
        UIParsing.expectAttributes(element, "direction");

        return switch (element.getAttribute("direction")) {
            case "horizontal" -> EContainers.selectableHorizontal(Sizing.content(), Sizing.content());
            case "ltr-text-flow" -> EContainers.selectableLtrTextFlow(Sizing.content(), Sizing.content());
            default -> EContainers.selectableVertical(Sizing.content(), Sizing.content());
        };
    }

    static {
        SELECTED_SURFACE = (context, component) -> {
            if (!(component instanceof SelectableLayout layout)) return;

            for (var child : layout.selected()) {
                context.drawRectOutline(child.x(), child.y(), child.width(), child.height(), 0xFFFFFFFF);
            }
        };
    }
}
