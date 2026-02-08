package fzmm.zailer.me.client.gui.options;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.gui.components.IMode;
import fzmm.zailer.me.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

import java.util.function.Supplier;

public enum BookOption implements IMode {
    CREATE_BOOK("createBook", () -> BookBuilder.builder().title(Component.translatable("fzmm.item.imagetext.book.title").getString())),
    ADD_PAGE("addPage", () -> {
        Minecraft client = Minecraft.getInstance();
        assert client.player != null;

        return BookBuilder.of(ItemUtils.from(InteractionHand.MAIN_HAND)).orElse(CREATE_BOOK.bookBuilderSupplier.get());
    });


    private final String name;
    private final Supplier<BookBuilder> bookBuilderSupplier;

    BookOption(String name, Supplier<BookBuilder> getBookSupplier) {
        this.name = name;
        this.bookBuilderSupplier = getBookSupplier;
    }

    @Override
    public String getTranslationKey() {
        return "fzmm.gui.option.book." + this.name;
    }

    public BookBuilder getBookBuilder() {
        return this.bookBuilderSupplier.get();
    }
}