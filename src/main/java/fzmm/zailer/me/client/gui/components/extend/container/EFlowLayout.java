package fzmm.zailer.me.client.gui.components.extend.container;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EFlowLayout extends FlowLayout {
    @Nullable
    private Surface hoveredSurface = null;
    private boolean isFocused = false;

    public EFlowLayout(Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm) {
        super(horizontalSizing, verticalSizing, algorithm);
    }

    public Surface styledPanel() {
//        boolean darkMode = FzmmClient.CONFIG.guiStyle.darkMode();
//        return darkMode ? Surface.DARK_PANEL : Surface.PANEL;
        return Surface.DARK_PANEL;
    }

    public Surface styledBackground() {
        boolean useOldVanillaBackground = FzmmClient.CONFIG.guiStyle.oldBackground();
        return useOldVanillaBackground ? Surface.VANILLA_TRANSLUCENT : Surface.optionsBackground();
    }

    /**
     * this is necessary because it is not possible to compare the Surface
     * (because they are anonymous lambdas extending an interface)
     */
    private Optional<Surface> parseStyledSurface(Element surfaceElement) {
        List<Element> children = UIParsing.allChildrenOfType(surfaceElement, Node.ELEMENT_NODE);
        Surface result = Surface.BLANK;
        boolean modified = false;

        for (var child : children) {
            result = switch (child.getNodeName()) {
//                case "panel" -> {
//                    modified = true;
//                    yield result.and(styledPanel());
//                }
                case "options-background", "vanilla-translucent" -> {
                    modified = true;
                    yield result.and(styledBackground());
                }
                default -> result;
            };
        }

        return modified ? Optional.of(result) : Optional.empty();
    }

    public EFlowLayout hoveredSurface(@Nullable Surface hoveredSurface) {
        this.hoveredSurface = hoveredSurface;
        return this;
    }

    public <T extends Component> T childByIdOrThrow(@NotNull Class<T> expectedClass, @NotNull String id) {
        T result = this.childById(expectedClass, id);
        if (result == null) {
            throw new NullPointerException(String.format("No '%s' found with component id '%s'", expectedClass.getSimpleName(), id));
        }

        return result;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "surface", this::parseStyledSurface, surfaceOptional -> surfaceOptional.ifPresent(this::surface));
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        this.surface.draw(context, this);

        if (this.hoveredSurface != null && (this.isInBoundingBox(mouseX, mouseY) || this.isFocused)) {
            this.hoveredSurface.draw(context, this);
        }

        this.drawChildren(context, mouseX, mouseY, partialTicks, delta, this.children);
    }

    @Override
    public void onFocusGained(FocusSource source) {
        super.onFocusGained(source);
        this.isFocused = true;
    }

    @Override
    public void onFocusLost() {
        super.onFocusLost();
        this.isFocused = false;
    }

    public static FlowLayout parse(Element element) {
        UIParsing.expectAttributes(element, "direction");

        return switch (element.getAttribute("direction")) {
            case "horizontal" -> EContainers.horizontalFlow(Sizing.content(), Sizing.content());
            case "ltr-text-flow" -> EContainers.ltrTextFlow(Sizing.content(), Sizing.content());
            default -> EContainers.verticalFlow(Sizing.content(), Sizing.content());
        };
    }

}
