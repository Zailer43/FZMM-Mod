package fzmm.zailer.me.client.gui.components.snack_bar;

import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
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
        this.zIndex(900);
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
        this.startTimeMillis = System.currentTimeMillis();

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
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden && !(client.currentScreen instanceof ISnackBarScreen)) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        if (this.timerComponent != null) {
            this.updateTimer(System.currentTimeMillis() - this.startTimeMillis);
        }
    }

    @Override
    public void setTimer(long timerMillis) {
        double configDisplayTime = MinecraftClient.getInstance().options.getNotificationDisplayTime().getValue();
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
    public void add(Component toast) {
        this.child(toast);
    }

    @Override
    public void setButtons(List<ButtonComponent> buttons) {
        this.buttons = buttons;
    }

    @Override
    public void buttonsEnabled(boolean value) {
        // dark_gray instead of gray because the background color of loading makes it not very visible
        Formatting color = value ? Formatting.WHITE : Formatting.DARK_GRAY;
        for (var button : this.buttons) {
            MutableText text = button.getMessage().copy();
            button.setMessage(text.setStyle(text.getStyle().withFormatting(color)));
            button.active = value;
        }
    }

    public static SnackBarBuilder builder(String id) {
        return SnackBarBuilder.builder(new BaseSnackBarComponent(Sizing.content(), Sizing.content()), id);
    }
}
