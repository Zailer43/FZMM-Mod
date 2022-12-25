package fzmm.zailer.me.exceptions;

public class BookNbtOverflow extends Exception {
    public static final int MAX_BOOK_NBT_SIZE = 0x4000;
    private final long bookNbtSize;

    public BookNbtOverflow(long bookNbtSize) {
        this.bookNbtSize = bookNbtSize;
    }

    public long getBookNbtSize() {
        return this.bookNbtSize;
    }
}
