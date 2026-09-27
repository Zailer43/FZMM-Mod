package fzmm.zailer.me.client.gui.utils.context_menu;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import io.wispforest.owo.ui.container.WrappingParentUIComponent;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CtxNode<T> extends CtxElement<T> {
    private final List<CtxElement<T>> children;
    private ICtxComponent<T> nodeBuilder;

    private CtxNode(String id, List<CtxElement<T>> children) {
        super(id);
        this.children = children;
        this.nodeBuilder = ICtxComponent.simpleNode();
    }

    public static <T> CtxNode<T> of(String id, List<CtxElement<T>> children) {
        return new CtxNode<>(id, children);
    }

    public static <T> CtxNode<T> of(String id) {
        return new CtxNode<>(id, new ArrayList<>());
    }

    public CtxNode<T> add(CtxElement<T> child) {
        this.children.add(child);
        return this;
    }

    protected List<? extends CtxElement<T>> normalizeDecorators(List<CtxElement<T>> elements) {
        var result = new ArrayList<CtxElement<T>>();
        for (var element : elements) {
            // can't have decorators twice in a row
            if (element.isDecorator() && !result.isEmpty() && result.get(result.size() - 1).isDecorator()) continue;
            result.add(element);
        }

        if (result.isEmpty()) return result;
        if (result.get(0).isDecorator()) {
            result.remove(0); // first can't be a decorator
        }

        if (result.isEmpty()) return result;
        if (result.get(result.size() - 1).isDecorator()) {
            result.remove(result.size() - 1); // last can't be a decorator
        }

        return result;
    }

    public ParentUIComponent createNode(CtxMenuManager<T> manager, T selection, CtxType type) {
        var result = this.nodeBuilder.build(this);
        if (!this.modeSupports(selection)) return result;

        var layout = this.findLayout(result).orElseThrow(() ->
                new IllegalStateException("Context Menu base layout should be a FlowLayout or ScrollContainer, but was a " + result.getClass().getSimpleName())
        );

        return this.setupNodeChildren(manager, result, layout, selection, type);
    }

    /**
     *
     */
    public Optional<EFlowLayout> findLayout(ParentUIComponent nodeLayout) {
        if (nodeLayout instanceof EFlowLayout layout) {
            return Optional.of(layout);
        } else if (nodeLayout instanceof WrappingParentUIComponent<?> container && container.child() instanceof EFlowLayout layout) {
            return Optional.of(layout);
        }

        return Optional.empty();
    }

    protected ParentUIComponent setupNodeChildren(CtxMenuManager<T> manager, ParentUIComponent node, EFlowLayout nodeLayout, T selection, CtxType type) {
        for (var child : this.supportedChildren(selection, type)) {
            nodeLayout.child(this.createNodeChild(manager, node, child, selection, type));
        }

        this.adjustNodeSizing(manager, node, nodeLayout);

        return node;
    }

    public List<? extends CtxElement<T>> supportedChildren(T selection, CtxType type) {
        var result = new ArrayList<CtxElement<T>>();

        for (var child : this.children) {
            try {
                if (child instanceof CtxNode<T> node && (!node.modeSupports(selection) || node.supportedChildren(selection, type).isEmpty())) {
                    continue;
                }
                if (!child.supports(selection, type)) continue;

                result.add(child);
            } catch (Exception e) {
                FzmmClient.LOGGER.warn("[CtxNode] Exception getting context menu entry '{}'", child.id, e);
            }
        }

        return this.normalizeDecorators(result);
    }


    /**
     * The node <b>elements</b> need to have an expanded size to properly support the hovered surface and click events.
     * For this to work, they need to be inside a layout with fixed sizing, otherwise, they will take
     * the size of the screen
     */
    private void adjustNodeSizing(CtxMenuManager<T> manager, ParentUIComponent nodeWrapper, EFlowLayout nodeLayout) {
        // Hack: Inflate the component so its width/height can be determined
        nodeWrapper.inflate(manager.layout.fullSize());
        if (nodeLayout.horizontalSizing().get().method != Sizing.Method.CONTENT) return;

        nodeLayout.horizontalSizing(Sizing.expand(100));
        // Hack: nodeWrapper, which contains nodeLayout (or is nodeLayout itself), must size itself to the fixed size of its content
        nodeWrapper.horizontalSizing(Sizing.fixed(nodeWrapper.width()));
        for (var child : nodeLayout.children()) {
            child.horizontalSizing(Sizing.expand(100));
        }
    }

    protected ParentUIComponent createNodeChild(CtxMenuManager<T> manager, ParentUIComponent menuLayout, CtxElement<T> element, T selection, CtxType type) {
        var result = element.createComponent();
        result.mouseDown().subscribe((click, doubled) -> {
            if (element.hasNested()) return true;

            element.execute(selection, type);
            if (!doubled) { // Hack: suppress the entry sound if a component already plays it
                UISounds.playButtonSound();
            }

            return manager.clearChildren();
        });
        result.mouseEnter().subscribe(() -> this.onEntryHovered(manager, element, menuLayout, selection, type, result.y()));
        result.keyPress().subscribe(input -> {
            var focusHandler = result.focusHandler();
            if (focusHandler == null) return false;

            if (input.isEscape()) return manager.clearChildren();
            if (input.isConfirmation()) {
                element.execute(selection, type);
                UISounds.playButtonSound();

                return manager.clearChildren();
            }
            if (input.isUp() || input.isDown() || input.isLeft() || input.isRight()) {
                if (input.isRight() && element.hasNested() && element instanceof CtxNode<T> node) {
                    this.onEntryHovered(manager, node, menuLayout, selection, type, result.y());
                }
                focusHandler.moveFocus(input.key());
                return true;
            }

            return false;
        });

        return result;
    }

    private void onEntryHovered(CtxMenuManager<T> manager, CtxElement<T> element, ParentUIComponent menuLayout, T selection, CtxType type, int y) {
        manager.clearMenusAfter(menuLayout);
        if (!(element.hasNested() && element instanceof CtxNode<T> node)) return;

        var nested = node.createNode(manager, selection, type);
        manager.child(menuLayout, nested, 0, y);
    }

    public CtxNode<T> nodeComponent(ICtxComponent<T> builder) {
        this.nodeBuilder = builder;
        return this;
    }

    @Override
    public boolean isDecorator() {
        return false;
    }

    @Override
    public boolean hasNested() {
        return !this.children.isEmpty();
    }

    @Override
    public boolean supports(T selection, CtxType type) {
        if (!super.supports(selection, type)) return false;

        for (var child : this.children) {
            if (child.supports(selection, type)) return true;
        }
        return false;
    }

    @Override
    public boolean modeSupports(T value) {
        return this.children.stream().anyMatch(element -> element.modeSupports(value));
    }

    @Override
    public CtxElement<T> execute(T selection, CtxType type) {
        return this;
    }
}
