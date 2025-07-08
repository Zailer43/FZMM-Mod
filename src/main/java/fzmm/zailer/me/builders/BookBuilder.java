package fzmm.zailer.me.builders;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookBuilder {

    private boolean resolved;
    private final List<RawFilteredPair<Text>> pages;
    private int generation;
    private String author;
    private RawFilteredPair<String> title;

    private BookBuilder() {
        this.resolved = false;
        this.pages = new ArrayList<>();
        this.generation = 0;
        assert MinecraftClient.getInstance().player != null;
        this.author = MinecraftClient.getInstance().player.getName().getString();
        this.title = null;
    }

    public static BookBuilder builder() {
        return new BookBuilder();
    }

    public static Optional<BookBuilder> of(ItemStack bookStack) {
        bookStack = bookStack.copy();
        WrittenBookContentComponent content = bookStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);

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

    public BookBuilder addPage(Text text) {
        this.pages.add(RawFilteredPair.of(text));
        return this;
    }

    public BookBuilder addFilteredPages(List<RawFilteredPair<Text>> pages) {
        this.pages.addAll(pages);
        return this;
    }

    public BookBuilder title(String title) {
        return this.title(RawFilteredPair.of(title));
    }

    public BookBuilder title(RawFilteredPair<String> filteredTitle) {
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
        DynamicRegistryManager registryManager = FzmmUtils.getRegistryManager();

        for (var pageFilteredPair : this.pages) {
            Text pageText = pageFilteredPair.raw();
            if (pageText != null && WrittenBookContentComponent.exceedsSerializedLengthLimit(pageText, registryManager)) {
                return TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, pageText).result()
                        .map(JsonElement::toString)
                        .map(String::length)
                        .orElse(Integer.MAX_VALUE); // Failed to encode
            }
        }

        return -1;
    }

    public ItemStack get() {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);


        stack.apply(DataComponentTypes.WRITTEN_BOOK_CONTENT, WrittenBookContentComponent.DEFAULT, component -> {
            RawFilteredPair<String> titleCopy = this.title;
            if (titleCopy.get(false).length() > WrittenBookContentComponent.MAX_TITLE_LENGTH) {
                titleCopy = RawFilteredPair.of(titleCopy.get(false)
                        .substring(0, WrittenBookContentComponent.MAX_TITLE_LENGTH));
            }

            return new WrittenBookContentComponent(titleCopy, this.author, this.generation, this.pages, this.resolved);
        });

        return stack;
    }
}
