package fzmm.zailer.me.client.gui.components.snack_bar;

import fzmm.zailer.me.client.gui.components.extend.component.EBooleanButton;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class SnackBarBuilder {
    private final ISnackBarComponent snackBar;
    private Color backgroundColor = EStyles.ALERT_SUCCESS_COLOR;
    private final LabelComponent title;
    private LabelComponent details;
    private final List<ButtonComponent> buttons = new ArrayList<>();
    private Sizing horizontalSizing = Sizing.content();
    private Sizing verticalSizing = Sizing.content();
    private boolean closeButton = false;
    private boolean timerEnabled = false;
    private boolean detailsExpanded = false;
    private boolean keepOnLimit = false;
    private long timerMillis = -1;
    private String id = "generic";

    private SnackBarBuilder(ISnackBarComponent snackBar, LabelComponent title, LabelComponent details) {
        this.snackBar = snackBar;
        this.title = title;
        this.details = details;
    }

    public static SnackBarBuilder builder(ISnackBarComponent snackBar, String id) {
        return builder(snackBar, EComponents.label(Text.empty()), null, id);
    }

    public static SnackBarBuilder builder(ISnackBarComponent snackBar, LabelComponent title, LabelComponent details, String id) {
        return new SnackBarBuilder(snackBar, title, details).id(id);
    }

    public SnackBarBuilder title(Text text) {
        this.title.text(text);
        return this;
    }

    public SnackBarBuilder backgroundColor(Color color) {
        this.backgroundColor = color;
        return this;
    }

    public SnackBarBuilder details(Text details) {
        if (this.details == null) {
            this.details = EComponents.label(Text.empty());
        }

        this.details.text(details);
        return this;
    }
    
    public SnackBarBuilder expandDetails() {
        this.detailsExpanded = true;
        return this;
    }

    public SnackBarBuilder button(Function<ISnackBarComponent, ButtonComponent> function) {
        ButtonComponent button = function.apply(this.snackBar);
        button.verticalSizing(Sizing.fixed(16));
        button.margins(Insets.bottom(2));
        this.buttons.add(button);
        return this;
    }

    /**
     * This timer is typically used for displaying messages indicating a successful operation.
     * The duration for this timer is set to 5 seconds.
     */
    public SnackBarBuilder lowTimer() {
        return this.customTimer(5, TimeUnit.SECONDS);
    }

    /**
     * This timer is generally used for showing error messages.
     * The duration for this timer is set to 10 seconds.
     */
    public SnackBarBuilder mediumTimer() {
        return this.customTimer(10, TimeUnit.SECONDS);
    }

    /**
     * This timer is usually employed when a SnackBar contains clickable components, such as a button.
     * The duration for this timer is set to 20 seconds.
     */
    public SnackBarBuilder highTimer() {
        return this.customTimer(20, TimeUnit.SECONDS);
    }

    public SnackBarBuilder customTimer(long amount, TimeUnit unit) {
        this.timerMillis = TimeUnit.MILLISECONDS.convert(amount, unit);
        return this;
    }

    public SnackBarBuilder sizing(Sizing horizontalSizing, Sizing verticalSizing) {
        this.horizontalSizing = horizontalSizing;
        this.verticalSizing = verticalSizing;
        return this;
    }

    /**
     * By default, the timer is not started automatically. You can either start the timer manually
     * from the SnackBar after it has been built or use this method to enable it during the configuration.
     * When this option is enabled, the timer will begin as soon as the SnackBar is displayed.
     */
    public SnackBarBuilder startTimer() {
        this.timerEnabled = true;
        return this;
    }

    public SnackBarBuilder closeButton() {
        this.closeButton = true;
        return this;
    }

    /**
     * Configures the behavior of SnackBars when the display limit is reached.
     * With this option, will be kept on screen if the limit of visible SnackBars is reached.
     */
    public SnackBarBuilder keepOnLimit() {
        this.keepOnLimit = true;
        return this;
    }

    public SnackBarBuilder id(String id) {
        this.id = id;
        return this;
    }

    public ISnackBarComponent build() {
        ISnackBarComponent result = this.snackBar;
        List<ButtonComponent> buttons = new ArrayList<>(this.buttons);

        result.surface(Surface.flat(this.backgroundColor.argb()));

        FlowLayout layout = (FlowLayout) EContainers.verticalFlow(Sizing.content(), Sizing.content())
                .gap(1)
                .padding(Insets.of(3))
                .horizontalAlignment(HorizontalAlignment.LEFT);

        // first row (expand?, title, close?)
        FlowLayout firstRow = EContainers.ltrTextFlow(Sizing.expand(100), Sizing.content());
        firstRow.gap(2);
        firstRow.verticalAlignment(VerticalAlignment.CENTER);
        layout.child(firstRow);
        // second row (buttons?)
        if (!this.buttons.isEmpty()) {
            FlowLayout secondRow = EContainers.ltrTextFlow(Sizing.expand(100), Sizing.content());
            secondRow.children(this.buttons);
            secondRow.gap(4);
            layout.child(secondRow);
        }

        // first row expand button and third row with details
        if (this.details != null && !this.details.text().toString().isEmpty()) {
            FlowLayout detailsLayout = EContainers.verticalFlow(Sizing.content(), Sizing.content());
            layout.child(detailsLayout);

            EBooleanButton detailsButton = this.getDetailsButton(detailsLayout);
            buttons.add(detailsButton);
            firstRow.child(detailsButton);
        }
        // first row content
        firstRow.child(this.title.margins(Insets.vertical(2)));
        if (this.closeButton) {
            ButtonComponent button = Components.button(Text.translatable("fzmm.snack_bar.close"), buttonComponent -> result.close());
            button.sizing(Sizing.fixed(14));
            button.positioning(Positioning.relative(100, 0));
            button.renderer(ButtonComponent.Renderer.flat(0x00000000, EStyles.UNSELECTED_COLOR, 0x00000000));
            buttons.add(button);

            firstRow.horizontalSizing(Sizing.expand(100));
            firstRow.child(button);
        }

        if (this.horizontalSizing.isContent()) {
            int width = MinecraftClient.getInstance().textRenderer.getWidth(this.title.text());
            int firstRowChildrenSize = firstRow.children().size() - 1;
            width += 14 * firstRowChildrenSize;
            width += firstRow.gap() * firstRowChildrenSize;
            width += layout.padding().get().horizontal();

            this.horizontalSizing = Sizing.fixed(width);
        }

        layout.sizing(this.horizontalSizing, this.verticalSizing);
        result.sizing(this.horizontalSizing, this.verticalSizing);
        result.setTimer(this.timerMillis);
        result.id(this.id);
        result.add(layout);

        if (this.timerEnabled) {
            result.startTimer();
        }

        if (this.keepOnLimit) {
            this.snackBar.removeOnLimit(false);
        }

        result.setButtons(buttons);

        return result;
    }

    private EBooleanButton getDetailsButton(FlowLayout detailsLayout) {
        //TODO: animate collapsing button
        EBooleanButton button = new EBooleanButton(
                Text.translatable("fzmm.snack_bar.expand.expanded"),
                Text.translatable("fzmm.snack_bar.expand.collapsed")
        );
        button.renderer(ButtonComponent.Renderer.flat(0x00000000, EStyles.UNSELECTED_COLOR, 0x00000000));
        button.sizing(Sizing.fixed(14));
        this.details.horizontalSizing(Sizing.expand(100));
        button.onPress(buttonComponent -> {
            if (button.enabled()) {
                detailsLayout.child(this.details);
            } else {
                detailsLayout.removeChild(this.details);
            }
        });

        if (this.detailsExpanded) {
            button.enabled(true);
        }

        return button;
    }
}
