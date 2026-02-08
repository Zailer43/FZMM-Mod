package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.options.BookOption;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ImagetextBookTooltipTab implements IImagetextTab, IMemento {
    private ContextMenuButton bookTooltipButton;
    private BookOption bookMode;
    private TextBoxComponent bookTooltipAuthor;
    private TextAreaComponent bookTooltipMessage;

    @Override
    public void build(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.buildImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        String author = this.bookTooltipAuthor.getValue();
        String bookMessage = this.bookTooltipMessage.getValue();

        BookBuilder bookBuilder = this.bookMode.getBookBuilder()
                .author(author)
                .addPage(Component.literal(ChatFormatting.BLUE + bookMessage)
                        .setStyle(Style.EMPTY
                                .withHoverEvent(new HoverEvent.ShowText(logic.mergeText()))
                        )
                );

        ItemStack book = bookBuilder.get();

        int serializedLength = bookBuilder.exceedsSerializedLengthLimit();
        if (serializedLength != -1) {
            Minecraft.getInstance().execute(() -> {
                ISnackBarComponent toast = BaseSnackBarComponent.builder(SnackBarManager.IMAGETEXT_ID)
                        .title(Component.translatable("fzmm.snack_bar.bookTooltip.overflow.title", serializedLength, WrittenBookContent.PAGE_LENGTH))
                        .details(Component.translatable("fzmm.snack_bar.bookTooltip.overflow.details"))
                        .backgroundColor(EStyles.ALERT_ERROR_COLOR)
                        .keepOnLimit()
                        .highTimer()
                        .startTimer()
                        .closeButton()
                        .build();
                SnackBarManager.getInstance().add(toast);
            });
            return;
        }

        ItemUtils.give(book);
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        assert Minecraft.getInstance().player != null;
        this.bookTooltipButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "bookTooltipMode");
        this.bookTooltipButton.setContextMenuOptions(dropdownComponent -> {
            for (var option : BookOption.values()) {
                dropdownComponent.button(Component.translatable(option.getTranslationKey()), dropdownButton -> {
                    this.updateBookTooltip(option);
                    dropdownButton.remove();
                });
            }
        });
        this.updateBookTooltip(BookOption.ADD_PAGE);
        this.bookTooltipAuthor = TextBoxRow.setup(rootComponent, "bookTooltipAuthor", Minecraft.getInstance().player.getName().getString(), 512);
        this.bookTooltipMessage = rootComponent.childByIdOrThrow(TextAreaComponent.class, "bookTooltipMessage-text-area");
        this.bookTooltipMessage.maxLines(14);
        this.bookTooltipMessage.setCharacterLimit(4096);
        this.bookTooltipMessage.text(FzmmClient.CONFIG.imagetext.defaultBookMessage());

        FlowLayout layout = rootComponent.childByIdOrThrow(FlowLayout.class, "bookTooltipMessage-text-area-parent");
        layout.verticalSizing(Sizing.content());
    }

    private void updateBookTooltip(BookOption mode) {
        this.bookMode = mode;
        this.bookTooltipButton.setMessage(Component.translatable(this.bookMode.getTranslationKey()));
    }

    @Override
    public String getId() {
        return "bookTooltip";
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.bookTooltipAuthor.getValue());
        output.writeObject(this.bookTooltipMessage.getValue());
        output.writeObject(this.bookMode);
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.bookTooltipAuthor.text((String) input.readObject());
        this.bookTooltipMessage.text((String) input.readObject());
        this.updateBookTooltip((BookOption) input.readObject());
    }
}
