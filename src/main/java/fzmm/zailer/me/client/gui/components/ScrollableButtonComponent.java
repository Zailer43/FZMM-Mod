package fzmm.zailer.me.client.gui.components;

import io.wispforest.owo.mixin.ui.access.ClickableWidgetAccessor;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ScrollableButtonComponent extends ButtonComponent {
    public ScrollableButtonComponent(Text message, Consumer<ButtonComponent> onPress) {
        super(message, onPress);
    }

    /**
     * copy of {@link ButtonComponent#renderWidget(DrawContext, int, int, float)} but with drawScrollableText
     */
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderer.draw((OwoUIDrawContext) context, this, delta);

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int color = this.active ? 0xffffff : 0xa0a0a0;

        this.drawMessage(context, textRenderer, color);

        var tooltip = ((ClickableWidgetAccessor) this).owo$getTooltip();
        if (this.hovered && tooltip != null)
            context.drawTooltip(textRenderer, tooltip.getLines(MinecraftClient.getInstance()), HoveredTooltipPositioner.INSTANCE, mouseX, mouseY);
    }


}
