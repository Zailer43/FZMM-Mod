package fzmm.zailer.me.utils.list;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ListUtils {
    public static <VALUE, LIST extends IListEntry<VALUE>> void moveEntryInUnmodifiableList(List<LIST> list, int entryIndex, int direction, Runnable callback) {
        if (direction == -1 && entryIndex > 0) {
            VALUE previousEntry = list.get(entryIndex - 1).getValue();
            VALUE entry = list.get(entryIndex).getValue();

            list.get(entryIndex - 1).setValue(entry);
            list.get(entryIndex).setValue(previousEntry);
        } else if (direction == 1 && entryIndex < list.size() - 1) {
            VALUE previousEntry = list.get(entryIndex + 1).getValue();
            VALUE entry = list.get(entryIndex).getValue();

            list.get(entryIndex + 1).setValue(entry);
            list.get(entryIndex).setValue(previousEntry);
        }

        if (callback != null)
            callback.run();
    }

    public static <VALUE, LIST extends IListEntry<VALUE>> void upEntry(List<? extends LIST> list, LIST entry, @Nullable Runnable callback) {
        int entryIndex = list.indexOf(entry);
        moveEntryInUnmodifiableList(list, entryIndex, -1, callback);
    }

    public static <VALUE, LIST extends IListEntry<VALUE>> void downEntry(List<? extends LIST> list, LIST entry, @Nullable Runnable callback) {
        int entryIndex = list.indexOf(entry);
        moveEntryInUnmodifiableList(list, entryIndex, 1, callback);
    }
}