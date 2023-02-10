package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

import java.util.Optional;

public class BookBuilder {

    private boolean resolved;
    private final NbtList pages;
    private int generation;
    private String author;
    private String title;

    private BookBuilder() {
        this.resolved = false;
        this.pages = new NbtList();
        this.generation = 0;
        assert MinecraftClient.getInstance().player != null;
        this.author = MinecraftClient.getInstance().player.getName().getString();
        this.title = null;
    }

    public static BookBuilder builder() {
        return new BookBuilder();
    }

    public static Optional<BookBuilder> of(ItemStack book) {
        book = book.copy();
        if (!book.getItem().equals(Items.WRITTEN_BOOK) || !book.hasNbt())
            return Optional.empty();
        NbtCompound tag = book.getNbt();
        assert tag != null;

        String title = tag.getString(WrittenBookItem.TITLE_KEY);
        String author = tag.getString(WrittenBookItem.AUTHOR_KEY);
        boolean resolved = tag.getBoolean(WrittenBookItem.RESOLVED_KEY);
        int generation = tag.getInt(WrittenBookItem.GENERATION_KEY);
        NbtList pages = tag.getList(WrittenBookItem.PAGES_KEY, NbtElement.STRING_TYPE);

        return Optional.of(builder()
                .title(title)
                .author(author)
                .resolved(resolved)
                .generation(generation)
                .addPages(pages)
        );
    }

    public BookBuilder addPage(NbtString text) {
        this.pages.add(text);
        return this;
    }

    public BookBuilder addPage(Text text) {
        this.addPage(FzmmUtils.toNbtString(text, false));
        return this;
    }

    public BookBuilder addPages(NbtList pages) {
        this.pages.addAll(pages);
        return this;
    }

    public BookBuilder title(String title) {
        this.title = title;
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

    public ItemStack get() {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        NbtCompound tag = new NbtCompound();

        tag.putBoolean(WrittenBookItem.RESOLVED_KEY, this.resolved);
        tag.put(WrittenBookItem.PAGES_KEY, this.pages);
        tag.putInt(WrittenBookItem.GENERATION_KEY, this.generation);
        tag.putString(WrittenBookItem.AUTHOR_KEY, this.author);
        if (this.title != null)
            tag.putString(WrittenBookItem.TITLE_KEY, this.title);

        stack.setNbt(tag);

        return stack;
    }
}
