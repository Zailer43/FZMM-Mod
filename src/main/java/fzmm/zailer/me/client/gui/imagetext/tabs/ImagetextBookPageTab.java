package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.options.BookOption;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLine;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ImagetextBookPageTab implements IImagetextTab, IMemento {
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
                dropdownComponent.button(Component.translatable(option.getTranslationKey()), dropdownButton -> {
                    this.updateBookPage(option);
                    dropdownButton.remove();
                });
            }
        });
        this.updateBookPage(BookOption.ADD_PAGE);
    }

    private void updateBookPage(BookOption bookMode) {
        this.bookMode = bookMode;
        this.bookPageButton.setMessage(Component.translatable(this.bookMode.getTranslationKey()));
    }

    @Override
    public String getId() {
        return "bookPage";
    }

    private int maxImageWidthFrom(@Nullable String characters) {
        if (characters == null) {
            characters = ImagetextLine.DEFAULT_TEXT;
        }

        int maxTextWidth = BookViewScreen.TEXT_WIDTH - 1;
        int width = 0;
        Font textRenderer = Minecraft.getInstance().font;

        if (characters.length() == 1) {
            width = maxTextWidth / textRenderer.width(characters);
        } else {
            String message = "";
            int length = characters.length();
            do {
                message += characters.charAt(width % length);
                width++;
            } while (textRenderer.width(message) < maxTextWidth);
        }

        return width;
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.bookMode);
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.updateBookPage((BookOption) input.readObject());
    }
}
