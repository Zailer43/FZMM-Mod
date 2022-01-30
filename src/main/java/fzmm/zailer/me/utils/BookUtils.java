package fzmm.zailer.me.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

import javax.annotation.Nullable;

public class BookUtils {

    private final boolean resolved;
    private final NbtList pages;
    private final int generation;
    private final String author;
    private final String title;

    public BookUtils(String title, String author) {
        this(title, author, false, 0, new NbtList());
    }

    public BookUtils(String title, String author, boolean resolved, int generation, NbtList pages) {
        this.resolved = resolved;
        this.pages = pages;
        this.generation = generation;
        this.author = author;
        this.title = title;
    }

    @Nullable
    public static BookUtils of(ItemStack book) {
        if (!book.getItem().equals(Items.WRITTEN_BOOK) || !book.hasNbt())
            return null;
        NbtCompound tag = book.getNbt();
        assert tag != null;

        String title = tag.getString(WrittenBookItem.TITLE_KEY);
        String author = tag.getString(WrittenBookItem.AUTHOR_KEY);
        boolean resolved = tag.getBoolean(WrittenBookItem.RESOLVED_KEY);
        int generation = tag.getInt(WrittenBookItem.GENERATION_KEY);
        NbtList pages = tag.getList(WrittenBookItem.PAGES_KEY, NbtElement.STRING_TYPE);

        return new BookUtils(title, author, resolved, generation, pages);
    }

    public void addPage(NbtString text) {
        this.pages.add(text);
    }

    public void addPage(Text text) {
        this.addPage(FzmmUtils.textToNbtString(text, false));
    }

    public ItemStack get() {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        NbtCompound tag = new NbtCompound();

        tag.putBoolean(WrittenBookItem.RESOLVED_KEY, this.resolved);
        tag.put(WrittenBookItem.PAGES_KEY, this.pages);
        tag.putInt(WrittenBookItem.GENERATION_KEY, this.generation);
        tag.putString(WrittenBookItem.AUTHOR_KEY, this.author);
        tag.putString(WrittenBookItem.TITLE_KEY, this.title);
        stack.setNbt(tag);

        return stack;
    }
}
