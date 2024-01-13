package fzmm.zailer.me.client.gui.main.components;

import fzmm.zailer.me.client.gui.main.MainIcon;
import io.wispforest.owo.mixin.ui.access.ClickableWidgetAccessor;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class MainButtonComponent extends ButtonComponent {

    private MainIcon icon;

    public MainButtonComponent(Text message, Consumer<ButtonComponent> onPress) {
        super(message, onPress);
        this.icon = null;
        this.renderer(Renderer.flat(0x40000000, 0x20200000, 0x70000000));
    }


    // this is copied from ButtonComponent to change text height
    @Override
    public void renderWidget(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        OwoUIDrawContext context = OwoUIDrawContext.of(drawContext);
        this.renderer.draw(context, this, delta);
        context.drawRectOutline(this.getX(), this.getY(), this.width, this.height, 0x20000000);

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int color = this.active ? 0xffffff : 0xa0a0a0;
        int centerX = this.getX() + this.width / 2;

        if (this.textShadow) {
            context.drawCenteredTextWithShadow(textRenderer, this.getMessage(), centerX, this.getY() + 10, color);
        } else {
            context.drawText(this.getMessage(), this.getX() + this.width / 2f - textRenderer.getWidth(this.getMessage()) / 2f, this.getY() + 10, color, Color.WHITE.argb());
        }

        if (this.icon != null)
            this.icon.render(context, centerX - this.icon.getWidth() / 2, this.getY() + 22, mouseX, mouseY, delta);

        var tooltip = ((ClickableWidgetAccessor)this).owo$getTooltip();
        if (this.hovered && tooltip != null)
            context.drawTooltip(textRenderer, tooltip.getLines(MinecraftClient.getInstance()), HoveredTooltipPositioner.INSTANCE, mouseX, mouseY);
    }

    public void setIcon(MainIcon icon) {
        this.icon = icon;
    }
}
