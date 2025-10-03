package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.options.BookOption;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLine;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ImagetextBookPageTab implements IImagetextTab {
    private ContextMenuButton bookPageButton;
    private BookOption bookMode;

    @Override
    public void build(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        ImagetextData modifiedData = new ImagetextData(data.image(),
                this.maxImageWidthFrom(algorithm.pixelExample()),
                15,
                data.smoothRescaling(),
                data.similarityThreshold()
        );
        logic.buildImagetext(algorithm, modifiedData);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        BookBuilder bookBuilder = this.bookMode.getBookBuilder();
        bookBuilder.addPage(logic.mergeText());

        ItemUtils.give(bookBuilder.get());
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.bookPageButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "bookPageMode");
        this.bookPageButton.setContextMenuOptions(dropdownComponent -> {
            for (var option : BookOption.values()) {
                dropdownComponent.button(Text.translatable(option.getTranslationKey()), dropdownButton -> {
                    this.updateBookPage(option);
                    dropdownButton.remove();
                });
            }
        });
        this.updateBookPage(BookOption.ADD_PAGE);
    }

    private void updateBookPage(BookOption bookMode) {
        this.bookMode = bookMode;
        this.bookPageButton.setMessage(Text.translatable(this.bookMode.getTranslationKey()));
    }

    @Override
    public String getId() {
        return "bookPage";
    }

    private int maxImageWidthFrom(@Nullable String characters) {
        if (characters == null) {
            characters = ImagetextLine.DEFAULT_TEXT;
        }

        int maxTextWidth = BookScreen.MAX_TEXT_WIDTH - 1;
        int width = 0;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        if (characters.length() == 1) {
            width = maxTextWidth / textRenderer.getWidth(characters);
        } else {
            String message = "";
            int length = characters.length();
            do {
                message += characters.charAt(width % length);
                width++;
            } while (textRenderer.getWidth(message) < maxTextWidth);
        }

        return width;
    }

    @Override
    public IMementoObject createMemento() {
        return new BookPageMementoTab(this.bookMode);
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        BookPageMementoTab memento = (BookPageMementoTab) mementoTab;
        this.updateBookPage(memento.mode);
    }

    private record BookPageMementoTab(BookOption mode) implements IMementoObject {
    }
}
