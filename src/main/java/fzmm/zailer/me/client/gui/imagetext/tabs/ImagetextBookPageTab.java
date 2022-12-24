package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.row.EnumRow;
import fzmm.zailer.me.client.gui.imagetext.IImagetextTab;
import fzmm.zailer.me.client.gui.imagetext.ImagetextBookOption;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.BookScreen;

public class ImagetextBookPageTab implements IImagetextTab {
    private static final String BOOK_PAGE_MODE_ID = "bookPageMode";
    private EnumWidget bookPageMode;

    @Override
    public void execute(ImagetextLogic logic) {
        ImagetextBookOption bookOption = (ImagetextBookOption) this.bookPageMode.getValue();

        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        BookBuilder bookBuilder = bookOption.getBookBuilder();
        bookBuilder.addPage(logic.getText());

        FzmmUtils.giveItem(bookBuilder.get());
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.bookPageMode = EnumRow.setup(rootComponent, BOOK_PAGE_MODE_ID, ImagetextBookOption.ADD_PAGE, null);
    }

    @Override
    public String getId() {
        return "bookPage";
    }

    public static int getMaxImageWidthForBookPage(String characters) {
        int maxTextWidth = BookScreen.MAX_TEXT_WIDTH - 1;
        int width = 0;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        if (characters.length() == 1)
            width = maxTextWidth / textRenderer.getWidth(characters);
        else {
            String message = "";
            int length = characters.length();
            do {
                message += characters.charAt(width % length);
                width++;
            } while (textRenderer.getWidth(message) < maxTextWidth);
        }

        return width;
    }
}
