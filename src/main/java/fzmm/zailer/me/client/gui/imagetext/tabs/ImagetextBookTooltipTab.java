package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.imagetext.IImagetextTab;
import fzmm.zailer.me.client.gui.imagetext.ImagetextBookOption;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.client.toast.BookNbtOverflowToast;
import fzmm.zailer.me.exceptions.BookNbtOverflow;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ImagetextBookTooltipTab implements IImagetextTab {
    private static final String BOOK_TOOLTIP_MODE_ID = "bookTooltipMode";
    private static final String BOOK_TOOLTIP_AUTHOR_ID = "bookTooltipAuthor";
    private static final String BOOK_TOOLTIP_MESSAGE_ID = "bookTooltipMessage";
    private EnumWidget bookTooltipMode;
    private TextFieldWidget bookTooltipAuthor;
    private TextFieldWidget bookTooltipMessage;

    @Override
    public void execute(ImagetextLogic logic) {
        ImagetextBookOption bookOption = (ImagetextBookOption) this.bookTooltipMode.getValue();
        String author = this.bookTooltipAuthor.getText();
        String bookMessage = this.bookTooltipMessage.getText();

        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            assert mc.player != null;
            BookBuilder bookBuilder = bookOption.getBookBuilder()
                    .author(author)
                    .addPage(Text.literal(Formatting.BLUE + bookMessage)
                            .setStyle(Style.EMPTY
                                    .withHoverEvent(HoverEvent.Action.SHOW_TEXT.buildHoverEvent(logic.getText())))
                    );

            ItemStack book = bookBuilder.get();
            assert book.getNbt() != null;

            long bookLength = FzmmUtils.getLength(book);
            if (bookLength > BookNbtOverflow.MAX_BOOK_NBT_SIZE)
                throw new BookNbtOverflow(bookLength);
            else
                FzmmUtils.giveItem(book);
        } catch (BookNbtOverflow e) {
            MinecraftClient.getInstance().getToastManager().add(new BookNbtOverflowToast(e));
        }
    }

    @Override
    public Component[] getComponents(BaseFzmmScreen parent) {
        return new Component[] {
                parent.newEnumRow(BOOK_TOOLTIP_MODE_ID),
                parent.newTextFieldRow(BOOK_TOOLTIP_AUTHOR_ID),
                parent.newTextFieldRow(BOOK_TOOLTIP_MESSAGE_ID)
        };
    }

    @Override
    public void setupComponents(BaseFzmmScreen parent, FlowLayout rootComponent) {
        this.bookTooltipMode = parent.setupEnum(rootComponent, BOOK_TOOLTIP_MODE_ID, ImagetextBookOption.ADD_PAGE, null);
        assert MinecraftClient.getInstance().player != null;
        this.bookTooltipAuthor = parent.setupTextField(rootComponent, BOOK_TOOLTIP_AUTHOR_ID, MinecraftClient.getInstance().player.getName().getString());
        this.bookTooltipMessage = parent.setupTextField(rootComponent, BOOK_TOOLTIP_MESSAGE_ID, FzmmClient.CONFIG.imagetext.defaultBookMessage());
    }

    @Override
    public String getId() {
        return "bookTooltip";
    }
}
