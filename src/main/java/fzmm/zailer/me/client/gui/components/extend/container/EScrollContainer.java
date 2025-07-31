package fzmm.zailer.me.client.gui.components.extend.container;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

public class EScrollContainer<C extends Component> extends ScrollContainer<C> {
    public static final int SCROLLBAR_THICCNESS = 5;
    protected boolean preventShiftScroll = false;
    protected boolean flipScroll = false;

    public EScrollContainer(ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, C child, boolean flipScroll) {
        super(direction, horizontalSizing, verticalSizing, child);

        this.flipScroll(flipScroll);
        this.scrollbarThiccness(SCROLLBAR_THICCNESS);
        this.scrollbar(this.styledScrollbar());

        // this is a workaround to solve that some components draw on top of the scroll,
        // causing that part not to be displayed correctly
        Insets oldPadding = this.padding.get();

        this.updateScrollPadding(oldPadding);
    }

    public ScrollContainer.Scrollbar styledScrollbar() {
        boolean isOverrideScrollbar = FzmmClient.CONFIG.guiStyle.persistentScrollbar();
        return isOverrideScrollbar ? this.vanillaFlat(this.flipScroll) : this.flat(Color.WHITE, this.flipScroll);
    }

    public boolean preventShiftScroll() {
        return preventShiftScroll;
    }

    public void preventShiftScroll(boolean isShiftRequired) {
        this.preventShiftScroll = isShiftRequired;
    }

    public boolean flipScroll() {
        return this.flipScroll;
    }

    private void flipScroll(boolean flip) {
        this.flipScroll = flip;
    }

    private void updateScrollPadding(Insets oldPadding) {
        if (this.direction == ScrollDirection.VERTICAL) {
            this.padding(this.flipScroll ? oldPadding.withLeft(this.scrollbarThiccness) : oldPadding.withRight(this.scrollbarThiccness));
        } else {
            this.padding(this.flipScroll ? oldPadding.withTop(this.scrollbarThiccness) : oldPadding.withBottom(this.scrollbarThiccness));
        }
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);

        this.scrollbarOffset = this.direction == ScrollDirection.VERTICAL ?
                this.getScrollbarX(this.flipScroll, this.scrollbarOffset) :
                this.getScrollbarY(this.flipScroll, this.scrollbarOffset);
    }

    /**
     * Copy of {@link io.wispforest.owo.ui.base.BaseParentComponent#drawChildren(OwoUIDrawContext, int, int, float, float, List)}
     * with workaround to owo-lib in 1.21.6 - 1.21.8
     */
    @Override
    protected void drawChildren(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta, List<? extends Component> children) {
        if (!this.allowOverflow) {
            var padding = this.padding.get();
            context.enableScissor(this.x + padding.left(), this.y + padding.top(), this.x + padding.left() + this.width - padding.horizontal(), this.y + padding.top() + this.height - padding.vertical());
        }

        var focusHandler = this.focusHandler();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < children.size(); i++) {
            final var child = children.get(i);
            int x = Math.max(child.x(), 0); // in 1.21.6 if x or y is less than 0 scissor is applied incorrectly
            int y = Math.max(child.y(), 0);
            if (child.width() <= 0 || child.height() <= 0) continue;

            if (!(context.scissorContains(x, y) || context.scissorContains(x + child.width(), y + child.height()))) continue;

            child.draw(context, mouseX, mouseY, partialTicks, delta);
            if (focusHandler.lastFocusSource() == FocusSource.KEYBOARD_CYCLE && focusHandler.focused() == child) {
                child.drawFocusHighlight(context, mouseX, mouseY, partialTicks, delta);
            }
        }

        if (!this.allowOverflow) {
            context.disableScissor();
        }
    }

    @Override
    protected boolean isInScrollbar(double mouseX, double mouseY) {
        return super.isInScrollbar(mouseX, mouseY) && this.direction.choose(mouseY, mouseX) <= this.scrollbarOffset + this.scrollbarThiccness;
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        if (this.preventShiftScroll && Screen.hasShiftDown()) {
            return false;
        }
        return super.onMouseScroll(mouseX, mouseY, amount);
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "prevent-shift-scroll", UIParsing::parseBool, this::preventShiftScroll);
    }

    public static EScrollContainer<?> parse(Element element) {
        boolean flipScroll = element.hasAttribute("flip-scroll") &&
                element.getAttribute("flip-scroll").equals("true");

        return element.getAttribute("direction").equals("vertical")
                ? EContainers.verticalScroll(Sizing.content(), Sizing.content(), null, flipScroll)
                : EContainers.horizontalScroll(Sizing.content(), Sizing.content(), null, flipScroll);
    }

    /**
     * Identical to ScrollbarContainer#flat but prevents the scrollbar
     * from being overlapped by another surface and flip the scrollbar
     */
    public Scrollbar flat(Color color, boolean flipScroll) {
        int scrollbarColor = color.argb();

        return (context, x, y, width, height, trackX, trackY, trackWidth, trackHeight, lastInteractTime, direction, active) -> {
            if (!active) return;

            if (direction == ScrollDirection.HORIZONTAL) {
                y = this.getScrollbarY(flipScroll, y);
            } else {
                x = this.getScrollbarX(flipScroll, x);
            }

            final var progress = Easing.SINE.apply(MathHelper.clamp(lastInteractTime - Util.getEpochTimeMs(), 0, 750) / 750f);
            int alpha = (int) (progress * (scrollbarColor >>> 24));

            context.fill(
                    x, y, x + width, y + height,
                    alpha << 24 | (scrollbarColor & 0xFFFFFF)
            );
        };
    }

    /**
     * Identical to ScrollbarContainer#vanillaFlat but hides if it can't scroll because the child is not exceeded
     * and prevents the scrollbar from being overlapped by another surface and flip the scrollbar
     */
    public Scrollbar vanillaFlat(boolean flipScroll) {
        return (context, x, y, width, height, trackX, trackY, trackWidth, trackHeight, lastInteractTime, direction, active) -> {
            if ((direction != ScrollDirection.HORIZONTAL || width >= trackWidth) &&
                    (direction != ScrollDirection.VERTICAL || height >= trackHeight)) {
                return;
            }

            if (direction == ScrollDirection.HORIZONTAL) {
                y = this.getScrollbarY(flipScroll, y);
                trackY = this.getScrollbarY(flipScroll, trackY);
            } else {
                x = this.getScrollbarX(flipScroll, x);
                trackX = this.getScrollbarX(flipScroll, trackX);
            }

            context.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, Color.BLACK.argb());
            NinePatchTexture.draw(FLAT_VANILLA_SCROLLBAR_TEXTURE, context, x, y, width, height);
        };
    }

    private int getScrollbarX(boolean flipScroll, int x) {
        return flipScroll ? this.x() : x + this.scrollbarThiccness;
    }

    private int getScrollbarY(boolean flipScroll, int y) {
        return flipScroll ? this.y() : y + this.scrollbarThiccness;
    }

}
