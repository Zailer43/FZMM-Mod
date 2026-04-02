package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.mixin.component.book.MultilineTextFieldAccessor;
import fzmm.zailer.me.mixin_interfaces.IMultiLineBoxColorFix;
import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.component.WritableBookContent;

import java.util.ArrayList;
import java.util.List;

public class BookComponent extends TextAreaComponent {

    public BookComponent() {
        // vanilla book screen parity

        // rightWidgetOffset = 9
        // text widget width diff = SCROLLBAR_WIDTH + 2 = 8
        // bottomWidgetOffset = displayCharCount offset = 4
        super(Sizing.fixed(BookViewScreen.TEXT_WIDTH + 9 + 8), Sizing.fixed(BookViewScreen.TEXT_HEIGHT + 4));
        ((IMultiLineBoxColorFix) this).fzmm$textColor(0xFF000000);
        ((IMultiLineBoxColorFix) this).fzmm$cursorColor(0xFF222222);

        int rightWidgetOffset = 9;
        int bottomWidgetOffset = 4;
        int top = 26; // matching vanilla
        int bottom = BookViewScreen.IMAGE_HEIGHT - BookViewScreen.TEXT_HEIGHT - top - bottomWidgetOffset;
        int left = 32; // matching vanilla
        int right = BookViewScreen.IMAGE_WIDTH - BookViewScreen.TEXT_WIDTH - left - SCROLLBAR_WIDTH - 2 - rightWidgetOffset;
        // 192x192 and align text with texture
        this.margins(Insets.of(top, bottom, left, right));

        this.maxLines(BookViewScreen.TEXT_HEIGHT / Minecraft.getInstance().font.lineHeight);

        this.editBox.setCharacterLimit(Integer.MAX_VALUE); // remove display of max length (EditBoxWidget#renderOverlay)
        this.editBox.setValueListener(this::textChange);

        this.setCharacterLimit(WritableBookContent.PAGE_EDIT_LENGTH - 1);
    }

    @Override
    protected void extractBackground(GuiGraphicsExtractor graphics) {
        Insets margins = this.margins().get();
        graphics.blit(RenderPipelines.GUI_TEXTURED, BookViewScreen.BOOK_LOCATION, this.x() - margins.left(), this.y() - margins.top(), 0, 0, BookViewScreen.IMAGE_WIDTH, BookViewScreen.IMAGE_HEIGHT, 256, 256);
    }

    @Override
    protected void extractDecorations(GuiGraphicsExtractor graphics) {
        boolean displayCharCount = this.displayCharCount.get();
        this.displayCharCount(false); // remove display of display char count (TextAreaComponent#renderOverlay)

        super.extractDecorations(graphics);

        this.displayCharCount(displayCharCount);
        this.extractDisplayCharCount(graphics);
    }

    /**
     * copy of render of display char count in TextAreaComponent#renderOverlay with other position
     */
    protected void extractDisplayCharCount(GuiGraphicsExtractor graphics) {
        if (!this.displayCharCount.get()) return;

        var text = this.editBox.hasCharacterLimit()
                ? Component.translatable("gui.multiLineEditBox.character_limit", this.editBox.value().length(), this.editBox.characterLimit())
                : Component.literal(String.valueOf(this.editBox.value().length()));

        var textRenderer = Minecraft.getInstance().font;
        graphics.text(textRenderer, text, this.getX() + this.width - textRenderer.width(text) - 14, this.getY() - 12, 0xA0A0A0);
    }

    @Override
    protected boolean scrollable() {
        return false;
    }

    @Override
    public int heightOffset() {
        return 0;
    }

    @Override
    public int widthOffset() {
        return 0;
    }

    private void textChange(String text) {
        // remove overflow text because edit box does not allow to disable it
        int overflow = this.editBox.getLineCount() - this.maxLines();
        if (overflow <= 0) return;
        StringSplitter textHandler = Minecraft.getInstance().font.getSplitter();
        int cursor = this.editBox.cursor();
        String modifiedText = text;
        int difference = 0;
        int editBoxWidth = ((MultilineTextFieldAccessor) this.editBox).getWidth();
        // as I cannot know where the cursor was before the text changed,
        // the characters are removed until the text enters into the line limit,
        // this is preferable to deleting the last line in case text is added
        // in the middle of the book and exceeds the maximum number of lines.
        while (textHandler.splitLines(modifiedText, editBoxWidth, Style.EMPTY).size() > this.maxLines()) {
            modifiedText = this.removeChar(modifiedText, this.getIndex(modifiedText, cursor, difference));
        }

        int index = this.getIndex(modifiedText, cursor, difference);
        if (modifiedText.equals(text) && modifiedText.charAt(index - 1) == '\n') {
            modifiedText = this.removeChar(modifiedText, index);
            difference--;
        }

        if (!modifiedText.equals(text)) {
            this.text(modifiedText);
            this.editBox.seekCursor(Whence.ABSOLUTE, cursor - difference - 1);
        }
    }

    private int getIndex(String text, int cursor, int difference) {
        return Math.min(Math.abs(cursor - difference), text.length());
    }

    private String removeChar(String text, int index) {
        return text.substring(0, index - 1) + text.substring(index);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return false;
    }

    public List<String> getWrappedText() {
        List<String> result = new ArrayList<>();
        String text = this.editBox.value();
        int count = this.editBox.getLineCount();

        for (int i = 0; i < count; i++) {
            MultilineTextField.StringView substring = this.editBox.getLineView(i);
            String line = text.substring(substring.beginIndex(), substring.endIndex());
            result.add(line.replaceAll("\\s+$", "")); // remove spaces at the end
        }

        return result;
    }
}
