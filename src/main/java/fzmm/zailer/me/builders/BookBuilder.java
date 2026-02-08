package fzmm.zailer.me.builders;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookBuilder {

    private boolean resolved;
    private final List<Filterable<Component>> pages;
    private int generation;
    private String author;
    private Filterable<String> title;

    private BookBuilder() {
        this.resolved = false;
        this.pages = new ArrayList<>();
        this.generation = 0;
        assert Minecraft.getInstance().player != null;
        this.author = Minecraft.getInstance().player.getName().getString();
        this.title = null;
    }

    public static BookBuilder builder() {
        return new BookBuilder();
    }

    public static Optional<BookBuilder> of(ItemStack bookStack) {
        bookStack = bookStack.copy();
        WrittenBookContent content = bookStack.get(DataComponents.WRITTEN_BOOK_CONTENT);

        if (content == null) {
            return Optional.empty();
        }

        return Optional.of(builder()
                .title(content.title())
                .author(content.author())
                .resolved(content.resolved())
                .generation(content.generation())
                .addFilteredPages(content.pages())
        );
    }

    public BookBuilder addPage(Component text) {
        this.pages.add(Filterable.passThrough(text));
        return this;
    }

    public BookBuilder addFilteredPages(List<Filterable<Component>> pages) {
        this.pages.addAll(pages);
        return this;
    }

    public BookBuilder title(String title) {
        return this.title(Filterable.passThrough(title));
    }

    public BookBuilder title(Filterable<String> filteredTitle) {
        this.title = filteredTitle;
        return this;
    }

    public BookBuilder author(String author) {
        this.author = author;
        return this;
    }

    public BookBuilder resolved(boolean resolved) {
        this.resolved = resolved;
        return this;
    }

    public BookBuilder generation(int generation) {
        this.generation = generation;
        return this;
    }

    /**
     * Checks if the book exceeds the serialized length limit
     * @return -1 if the book does not exceed the limit, {@link Integer#MAX_VALUE} if failed to encode, otherwise the length
     */
    public int exceedsSerializedLengthLimit() {
        RegistryAccess registryManager = FzmmUtils.getRegistryManager();

        for (var pageFilteredPair : this.pages) {
            Component pageText = pageFilteredPair.raw();
            if (pageText != null && WrittenBookContent.isPageTooLarge(pageText, registryManager)) {
                return ComponentSerialization.CODEC.encodeStart(FzmmUtils.getRegistryOps(JsonOps.INSTANCE), pageText).result()
                        .map(JsonElement::toString)
                        .map(String::length)
                        .orElse(Integer.MAX_VALUE); // Failed to encode
            }
        }

        return -1;
    }

    public ItemStack get() {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);


        stack.update(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY, component -> {
            Filterable<String> titleCopy = this.title;
            if (titleCopy.get(false).length() > WrittenBookContent.TITLE_MAX_LENGTH) {
                titleCopy = Filterable.passThrough(titleCopy.get(false)
                        .substring(0, WrittenBookContent.TITLE_MAX_LENGTH));
            }

            return new WrittenBookContent(titleCopy, this.author, this.generation, this.pages, this.resolved);
        });

        return stack;
    }
}
