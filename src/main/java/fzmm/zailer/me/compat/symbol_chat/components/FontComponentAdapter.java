package fzmm.zailer.me.compat.symbol_chat.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.compat.CompatMods;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.replaceitem.symbolchat.gui.container.ScrollableGridContainer;
import net.replaceitem.symbolchat.gui.widget.DropDownWidget;
import net.replaceitem.symbolchat.resource.FontProcessor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class FontComponentAdapter extends VanillaWidgetComponent {
    protected final CustomDropDownWidget widget;
    protected final ScrollableGridContainer scrollGrid;
    private final int expandedHeight;

    public FontComponentAdapter(CustomDropDownWidget widget, int expandedHeight) {
        super(widget);

        this.widget = widget;
        this.widget.visible = true;
        this.expandedHeight = expandedHeight;

        this.mouseDown().subscribe((mouseX, mouseY, button) -> {
            // ignore collapse
            this.expand();

            // fix click in the background
            return true;
        });

        this.zIndex(500);

        this.scrollGrid = this.getScrollableGrid();
        if (this.scrollGrid == null) {
            FzmmClient.LOGGER.warn("[FontComponentAdapter] Failed to get scrollable grid");
        }

        this.verticalSizing(Sizing.fixed(expandedHeight));
        this.expand();
    }

    protected void expand() {
        this.widget.expanded = true;
        // fix expanded
        if (this.scrollGrid != null) {
            this.scrollGrid.visible = true;
        }
        this.widget.setHeight(this.expandedHeight);
    }

    public void processFont(TextFieldWidget widget, String text, Consumer<String> writeConsumer) {
        try {
            FontProcessor selectedFont = this.widget.getSelection();

            text = selectedFont.convertString(text);
            writeConsumer.accept(text);

            if (selectedFont.isReverseDirection()) {
                int pos = widget.getCursor() - text.length();
                widget.setSelectionStart(pos);
                widget.setSelectionEnd(pos);
            }
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            CompatMods.SYMBOL_CHAT_PRESENT = false;
            FzmmClient.LOGGER.error("[FontComponentAdapter] Failed to process font", e);
        }
    }

    @Nullable
    private ScrollableGridContainer getScrollableGrid() {
        for (var child : this.widget.children()) {
            if (child instanceof ScrollableGridContainer) {
                return (ScrollableGridContainer) child;
            }
        }

        return null;
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        // fix scroll with smooth as it depends on the render
        // It is not being called because the custom implementation
        // of NonScrollableContainerWidget in Symbol Chat is not compatible with owo-lib by default
        this.widget.renderWidget(context, mouseX, mouseY, delta);
    }

    public static class CustomDropDownWidget extends DropDownWidget<FontProcessor> {

        public CustomDropDownWidget(int x, int y, int width, int height, List<FontProcessor> elementList, FontProcessor defaultSelection, boolean upward) {
            super(x, y, width, height, elementList, defaultSelection, upward);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
        }
    }
}
