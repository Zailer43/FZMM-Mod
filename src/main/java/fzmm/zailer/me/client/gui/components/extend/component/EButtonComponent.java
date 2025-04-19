package fzmm.zailer.me.client.gui.components.extend.component;

import io.wispforest.owo.mixin.ui.ClickableWidgetMixin;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;
import java.util.function.Function;

public class EButtonComponent extends ButtonComponent {
    private Function<Text, Text> messageProvider = message -> message;

    public EButtonComponent(Text message, Consumer<ButtonComponent> onPress) {
        super(message, onPress);
    }

    /**
     * Copy of {@link ButtonComponent#renderWidget(DrawContext, int, int, float)} but with {@link PressableWidget#drawScrollableText}
     */
    @Override
    public void renderWidget(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        OwoUIDrawContext context = (OwoUIDrawContext) drawContext;
        this.renderer.draw(context, this, delta);

        var textRenderer = MinecraftClient.getInstance().textRenderer;

        // drawScrollableText
        this.drawMessage(context, textRenderer, Color.ofFormatting(this.active() ? Formatting.WHITE : Formatting.GRAY).rgb());
    }

    /**
     * Copy of {@link ButtonComponent#shouldDrawTooltip(double, double)}, to avoid {@link ClickableWidgetMixin#shouldDrawTooltip(double, double)}
     * <p/>
     * Renders the tooltip even if {@link EButtonComponent#active()} is false
     */
    @Override
    public boolean shouldDrawTooltip(double mouseX, double mouseY) {
        // Renders the tooltip even if #active() is false
        return this.tooltip() != null && !this.tooltip().isEmpty() && this.isInBoundingBox(mouseX, mouseY);
    }

    @Override
    public void setMessage(Text message) {
        super.setMessage(this.messageProvider.apply(message));
    }

    public void setMessageProvider(Function<Text, Text> messageProvider) {
        this.messageProvider = messageProvider;
    }
}