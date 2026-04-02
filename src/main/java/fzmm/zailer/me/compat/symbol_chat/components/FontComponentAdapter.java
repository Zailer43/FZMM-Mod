package fzmm.zailer.me.compat.symbol_chat.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.compat.CompatMods;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.replaceitem.symbolchat.SymbolChat;
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

    private FontComponentAdapter(CustomDropDownWidget widget, int expandedHeight) {
        super(widget);

        this.widget = widget;
        this.widget.visible = true;
        this.expandedHeight = expandedHeight;

        this.mouseDown().subscribe((input, doubled) -> {
            // ignore collapse
            this.expand();

            // fix click in the background
            return true;
        });

        this.scrollGrid = this.getScrollableGrid();
        if (this.scrollGrid == null) {
            FzmmClient.LOGGER.warn("[FontComponentAdapter] Failed to get scrollable grid");
        }

        this.verticalSizing(Sizing.fixed(expandedHeight));
        this.expand();
    }

    public static FontComponentAdapter get() {
        FontComponentAdapter.CustomDropDownWidget widget = new FontComponentAdapter.CustomDropDownWidget(0, 0, 180, 15,
                SymbolChat.getFontManager().getFontProcessors(), SymbolChat.getFontManager().getCurrentScreenFontProcessor(), false);
        int expandedHeight = 150 + widget.getHeight(); // 150 is hardcoded in DropDownWidget
        widget.setHeight(expandedHeight);
        return new FontComponentAdapter(widget, expandedHeight);
    }

    protected void expand() {
        this.widget.expanded = true;
        // fix expanded
        if (this.scrollGrid != null) {
            this.scrollGrid.visible = true;
        }
        this.widget.setHeight(this.expandedHeight);
    }

    public void processFont(EditBox widget, String text, Consumer<String> writeConsumer) {
        try {
            FontProcessor selectedFont = this.widget.getSelection();

            text = selectedFont.convertString(text);
            writeConsumer.accept(text);

            if (selectedFont.isReverseDirection()) {
                int pos = widget.getCursorPosition() - text.length();
                widget.setCursorPosition(pos);
                widget.setHighlightPos(pos);
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
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        // fix scroll with smooth as it depends on the render
        // It is not being called because the custom implementation
        // of NonScrollableContainerWidget in Symbol Chat is not compatible with owo-lib by default
        this.widget.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
    }

    public static class CustomDropDownWidget extends DropDownWidget<FontProcessor> {

        public CustomDropDownWidget(int x, int y, int width, int height, List<FontProcessor> elementList, FontProcessor defaultSelection, boolean upward) {
            super(x, y, width, height, elementList, defaultSelection, upward);
        }

        // modify visibility
        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        }
    }
}
