package fzmm.zailer.me.client.gui.components.extend.component;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;

public class EButtonComponent extends ButtonComponent {
    private Function<Component, Component> messageProvider = message -> message;

    public EButtonComponent(Component message, Consumer<ButtonComponent> onPress) {
        super(message, onPress);
    }

    /**
     * Copy of {@link ButtonComponent#renderContents(GuiGraphics, int, int, float)} but with {@link AbstractButton#renderScrollingStringOverContents}
     */
    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        OwoUIGraphics graphics = (OwoUIGraphics) guiGraphics;
        this.renderer.draw(graphics, this, delta);

        // drawScrollableText
        this.renderScrollingStringOverContents(graphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE), this.getMessage(), 2);
    }

    /**
     * Copy of {@link ButtonComponent#shouldDrawTooltip(double, double)}, to avoid {@link Button#shouldDrawTooltip(double, double)}
     * <p/>
     * Renders the tooltip even if {@link EButtonComponent#active()} is false
     */
    @Override
    public boolean shouldDrawTooltip(double mouseX, double mouseY) {
        // Renders the tooltip even if #active() is false
        return this.tooltip() != null && !this.tooltip().isEmpty() && this.isInBoundingBox(mouseX, mouseY);
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(this.messageProvider.apply(message));
    }

    public void setMessageProvider(Function<Component, Component> messageProvider) {
        this.messageProvider = messageProvider;
    }

    public void onPress() {
        this.onPress(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)));
    }
}