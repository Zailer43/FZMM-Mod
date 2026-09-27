package fzmm.zailer.me.client.gui.utils.context_menu;

import com.mojang.blaze3d.platform.InputConstants;
import fzmm.zailer.me.client.gui.components.IconComponent;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ICtxComponent<T> {
    int LAYOUT_VERTICAL_PADDING = 3;
    int BACKGROUND_COLOR = 0xEB282828;
    int ACCENT_COLOR = 0xFF56AD56;

    ParentUIComponent build(CtxElement<T> element);

    private static EFlowLayout createEntryLayout() {
        var result = EContainers.horizontalFlow(Sizing.content(), Sizing.content());
        result.hoveredSurface(EStyles.DEFAULT_HOVERED)
                .gap(2)
                .verticalAlignment(VerticalAlignment.CENTER);
        return result;
    }

    private static UIComponent createNestedLabel(ParentUIComponent parent) {
        var result = EComponents.label(Component.literal(">").withColor(ICtxComponent.ACCENT_COLOR))
                .positioning(Positioning.relative(100, 50));

        var children = parent.children();
        if (!children.isEmpty()) {
            var lastChild = children.get(children.size() - 1);
            lastChild.margins(lastChild.margins().get().add(0, 0, 0, 10)); // avoid overlap with nested label
        }

        return result;
    }

    // === ENTRIES ===
    static <T> ICtxComponent<T> divider() {
        return element -> {
            // Hack: CtxNode#adjustNodeSizing should fix horizontal sizing
            var result = EContainers.horizontalFlow(Sizing.fixed(1), Sizing.fixed(3));
            result.padding(Insets.both(4, 1));

            result.child(UIComponents.box(Sizing.expand(100), Sizing.fixed(1))
                    .color(Color.ofArgb(ACCENT_COLOR)).fill(true)
            );
            return result;
        };
    }

    static <T> ICtxComponent<T> simple(Component label) {
        return simple(label, null);
    }

    static <T> ICtxComponent<T> simple(Component label, @Nullable Icon icon) {
        return element -> {
            var result = createEntryLayout();
            result.canKeyboardFocus(true)
                    .margins(Insets.horizontal(CtxMenuManager.NESTED_OVERLAP));

            if (icon != null) {
                result.child(new IconComponent(icon));
            }

            result.child(EComponents.label(label.copy().withColor(ACCENT_COLOR))
                    .margins(Insets.of(4, 3, 2, 2))
            );

            if (element.hasNested()) {
                result.child(createNestedLabel(result));
            } else {
                result.cursorStyle(CursorStyle.HAND);
                for (var child : result.children()) {
                    child.cursorStyle(CursorStyle.HAND);
                }
            }

            return result;
        };
    }

    static <T> ICtxComponent<T> button(Icon icon) {
        return element -> {
            var result = createEntryLayout();
            var entry = EComponents.button(Component.empty());

            entry.renderer((context, button, delta) -> {
                ButtonComponent.Renderer.VANILLA.draw(context, button, delta);
                icon.render(context, button.x() + 1, button.y() + 1, 0, 0, delta);
            }).onPress(buttonComponent -> {
                var buttonInfo = new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0);
                // Hack: Since this component is a button, it has its own execution sound,
                // but the entry also has one. To prevent the sound from playing twice,
                // set the 'doubled' flag in the mouse info to 'true'
                result.onMouseDown(new MouseButtonEvent(entry.x(), entry.y(), buttonInfo), true);
            }).sizing(Sizing.fixed(18));
            result.child(entry);

            return result;
        };
    }

    // === NODES ===

    private static void configNode(EFlowLayout layout, int backgroundColor, int accentColor) {
        layout.setOpaque()
                .padding(Insets.both(1, LAYOUT_VERTICAL_PADDING))
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .surface(Surface.blur(1, 2).and((context, component) -> {
                    context.fill(component.x(), component.y(),
                            component.x() + component.width(), component.y() + component.height(), backgroundColor
                    );
                    context.drawRectOutline(component.x(), component.y(), component.width(), component.height(), accentColor);
                }));
    }

    static <T> ICtxComponent<T> simpleNode() {
        return element -> {
            var result = EContainers.verticalFlow(Sizing.content(), Sizing.content());
            configNode(result, BACKGROUND_COLOR, ACCENT_COLOR);

            return result;
        };
    }

    static <T> ICtxComponent<T> ltrNode(Sizing horizontalSizing) {
        return element -> {
            EFlowLayout result = EContainers.ltrTextFlow(horizontalSizing, Sizing.content());
            configNode(result, BACKGROUND_COLOR, ACCENT_COLOR);

            return result;
        };
    }

    static <T> ICtxComponent<T> scrollNode(Sizing horizontalSizing, Sizing verticalSizing, int backgroundColor, int accentColor) {
        return element -> {
            EFlowLayout layout = EContainers.verticalFlow(Sizing.content(), Sizing.content());
            configNode(layout, backgroundColor, accentColor);

            return EContainers.verticalScroll(horizontalSizing, verticalSizing, layout);
        };
    }
}
