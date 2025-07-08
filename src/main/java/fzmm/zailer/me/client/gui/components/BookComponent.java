package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.mixin.component.book.EditBoxAccessor;
import fzmm.zailer.me.mixin_interfaces.IEditBoxTextColorFix;
import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.input.CursorMovement;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BookComponent extends TextAreaComponent {

    public BookComponent() {
        // vanilla book screen parity

        // rightWidgetOffset = 9
        // text widget width diff = SCROLLBAR_WIDTH + 2 = 8
        // bottomWidgetOffset = displayCharCount offset = 4
        super(Sizing.fixed(BookScreen.MAX_TEXT_WIDTH + 9 + 8), Sizing.fixed(BookScreen.MAX_TEXT_HEIGHT + 4));
        ((IEditBoxTextColorFix) this).fzmm$textColor(0xFF000000);
        ((IEditBoxTextColorFix) this).fzmm$cursorColor(0xFF222222);

        int rightWidgetOffset = 9;
        int bottomWidgetOffset = 4;
        int top = 26; // matching vanilla
        int bottom = BookScreen.HEIGHT - BookScreen.MAX_TEXT_HEIGHT - top - bottomWidgetOffset;
        int left = 32; // matching vanilla
        int right = BookScreen.WIDTH - BookScreen.MAX_TEXT_WIDTH - left - SCROLLBAR_WIDTH - 2 - rightWidgetOffset;
        // 192x192 and align text with texture
        this.margins(Insets.of(top, bottom, left, right));

        this.maxLines(BookScreen.MAX_TEXT_HEIGHT / MinecraftClient.getInstance().textRenderer.fontHeight);

        this.editBox.setMaxLength(Integer.MAX_VALUE); // remove display of max length (EditBoxWidget#renderOverlay)
        this.editBox.setChangeListener(this::textChange);

        this.setMaxLength(WritableBookContentComponent.MAX_PAGE_LENGTH - 1);
    }

    @Override
    protected void drawBox(DrawContext context) {
        Insets margins = this.margins().get();
        context.drawTexture(RenderPipelines.GUI_TEXTURED, BookScreen.BOOK_TEXTURE, this.x() - margins.left(), this.y() - margins.top(), 0, 0, BookScreen.WIDTH, BookScreen.HEIGHT, 256, 256);
    }

    @Override
    protected void renderOverlay(DrawContext context) {
        boolean displayCharCount = this.displayCharCount.get();
        this.displayCharCount(false); // remove display of display char count (TextAreaComponent#renderOverlay)

        super.renderOverlay(context);

        this.displayCharCount(displayCharCount);
        this.renderDisplayCharCount(context);
    }

    /**
     * copy of render of display char count in TextAreaComponent#renderOverlay with other position
     */
    protected void renderDisplayCharCount(DrawContext context) {
        if (!this.displayCharCount.get()) {
            return;
        }

        var text = this.editBox.hasMaxLength()
                ? Text.translatable("gui.multiLineEditBox.character_limit", this.editBox.getText().length(), this.editBox.getMaxLength())
                : Text.literal(String.valueOf(this.editBox.getText().length()));

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawTextWithShadow(textRenderer, text, this.getX() + this.width - textRenderer.getWidth(text) - 14, this.getY() - 12, 0xA0A0A0);
    }

    @Override
    protected boolean overflows() {
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
        if (overflow <= 0) {
            return;
        }
        TextHandler textHandler = MinecraftClient.getInstance().textRenderer.getTextHandler();
        int cursor = this.editBox.getCursor();
        String modifiedText = text;
        int difference = 0;
        int editBoxWidth = ((EditBoxAccessor) this.editBox).getWidth();
        // as I cannot know where the cursor was before the text changed,
        // the characters are removed until the text enters into the line limit,
        // this is preferable to deleting the last line in case text is added
        // in the middle of the book and exceeds the maximum number of lines.
        while (textHandler.wrapLines(modifiedText, editBoxWidth, Style.EMPTY).size() > this.maxLines()) {
            modifiedText = this.removeChar(modifiedText, this.getIndex(modifiedText, cursor, difference));
        }

        int index = this.getIndex(modifiedText, cursor, difference);
        if (modifiedText.equals(text) && modifiedText.charAt(index - 1) == '\n') {
            modifiedText = this.removeChar(modifiedText, index);
            difference--;
        }

        if (!modifiedText.equals(text)) {
            this.text(modifiedText);
            this.editBox.moveCursor(CursorMovement.ABSOLUTE, cursor - difference - 1);
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
        String text = this.editBox.getText();
        int count = this.editBox.getLineCount();

        for (int i = 0; i < count; i++) {
            EditBox.Substring substring = this.editBox.getLine(i);
            String line = text.substring(substring.beginIndex(), substring.endIndex());
            result.add(line.replaceAll("\\s+$", "")); // remove spaces at the end
        }

        return result;
    }
}
