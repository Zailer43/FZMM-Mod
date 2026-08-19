package fzmm.zailer.me.client.gui.components.snack_bar;

import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BaseSnackBarComponent extends EFlowLayout implements ISnackBarComponent {
    protected boolean timerEnabled;
    protected long timerMillis = -1;
    protected long startTimeMillis = 0;
    @Nullable
    protected FlowLayout timerComponent = null;
    protected boolean removeOnLimit = true;
    protected List<ButtonComponent> buttons = List.of();

    protected BaseSnackBarComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);
        this.timerEnabled = false;
    }

    @Override
    public ISnackBarComponent startTimer() {
        if (!this.timerEnabled) {
            FlowLayout timerLayout = EContainers.horizontalFlow(Sizing.expand(100), Sizing.fixed(2));
            timerLayout.positioning(Positioning.relative(0, 100));
            this.timerComponent = EContainers.horizontalFlow(Sizing.fixed(0), Sizing.expand(100));
            this.timerComponent.surface(Surface.flat(Color.WHITE.argb()));
            timerLayout.child(this.timerComponent);
            this.child(timerLayout);
        }

        this.timerEnabled = true;
        this.startTimeMillis = Util.getMillis();

        return this;
    }

    @Override
    public boolean removeOnLimit() {
        return this.removeOnLimit;
    }

    @Override
    public void removeOnLimit(boolean value) {
        this.removeOnLimit = value;
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.gui.hud.isHidden() && !(client.gui.screen() instanceof ISnackBarScreen)) return;
        super.draw(graphics, mouseX, mouseY, partialTicks, delta);
        if (this.timerComponent != null) {
            this.updateTimer(Util.getMillis() - this.startTimeMillis);
        }
    }

    @Override
    public void setTimer(long timerMillis) {
        double configDisplayTime = Minecraft.getInstance().options.notificationDisplayTime().get();
        this.timerMillis = (long) (timerMillis * configDisplayTime);
    }

    @Override
    public void updateTimer(long time) {
        if (!this.timerEnabled || this.timerMillis <= 0) {
            return;
        }

        float percent = time / (float) this.timerMillis;
        this.updateTimerBar(percent);

        if (time > this.timerMillis) {
            this.timerEnabled = false;
            this.close();
        }
    }

    @Override
    public void updateTimerBar(float percent) {
        int totalWidth = this.width();
        int width = (int) (totalWidth * percent);

        if (this.timerComponent != null) {
            this.timerComponent.horizontalSizing(Sizing.fixed(width));
        }
    }


    //TODO: add animation when add or close
    // animations algorithms: fade, slide

    @Override
    public void add(UIComponent toast) {
        this.child(toast);
    }

    @Override
    public void setButtons(List<ButtonComponent> buttons) {
        this.buttons = buttons;
    }

    @Override
    public void buttonsEnabled(boolean value) {
        // dark_gray instead of gray because the background color of loading makes it not very visible
        ChatFormatting color = value ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY;
        for (var button : this.buttons) {
            MutableComponent text = button.getMessage().copy();
            button.setMessage(text.setStyle(text.getStyle().applyFormat(color)));
            button.active = value;
        }
    }

    public static SnackBarBuilder builder(String id) {
        return SnackBarBuilder.builder(new BaseSnackBarComponent(Sizing.content(), Sizing.content()), id);
    }
}
