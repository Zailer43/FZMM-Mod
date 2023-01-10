package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.row.EnumRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.imagetext.IImagetextTab;
import fzmm.zailer.me.client.gui.imagetext.ImagetextBookOption;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.client.toast.BookNbtOverflowToast;
import fzmm.zailer.me.exceptions.BookNbtOverflow;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.FlowLayout;
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
    public void generate(ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.generateImagetext(data);
    }

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
    public void setupComponents(FlowLayout rootComponent) {
        this.bookTooltipMode = EnumRow.setup(rootComponent, BOOK_TOOLTIP_MODE_ID, ImagetextBookOption.ADD_PAGE, null);
        assert MinecraftClient.getInstance().player != null;
        this.bookTooltipAuthor = TextBoxRow.setup(rootComponent, BOOK_TOOLTIP_AUTHOR_ID, MinecraftClient.getInstance().player.getName().getString());
        this.bookTooltipMessage = TextBoxRow.setup(rootComponent, BOOK_TOOLTIP_MESSAGE_ID, FzmmClient.CONFIG.imagetext.defaultBookMessage());
    }

    @Override
    public String getId() {
        return "bookTooltip";
    }
}
