package fzmm.zailer.me.client.gui.utils.text_filter;

import io.wispforest.owo.ui.component.TextBoxComponent;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.BiPredicate;

public abstract class AbstractTextFilter<T, O> {
    public static final String DELIMITER = "=";
    public static final String EXCLUDE_MARKER = "-";
    private final String key;
    protected final BiPredicate<T, O> predicate;
    @Nullable
    protected T value = null;
    protected boolean isIncluded = true;

    public AbstractTextFilter(String key, BiPredicate<T, O> predicate) {
        this.key = key;
        this.predicate = predicate;
    }

    public static <O> String updateFilters(String searchText, Collection<AbstractTextFilter<?, O>> filterList) {
        for (var filter : filterList) {
            filter.reset();
        }

        Pair<String, List<String>> parsedSearch = parseSearch(searchText);
        List<String> filterSearchList = parsedSearch.getB();
        if (filterSearchList.isEmpty()) {
            return searchText;
        }

        for (var search : filterSearchList) {
            updateFilter(search, filterList);
        }

        return parsedSearch.getA();
    }

    private static <O> void updateFilter(String search, Collection<AbstractTextFilter<?, O>> filterList) {
        String[] split = search.split(DELIMITER);
        if (split.length != 2) return;

        String key = split[0].toLowerCase();
        String value = split[1];
        boolean isExclude = key.startsWith(EXCLUDE_MARKER);

        if (isExclude) {
            key = key.substring(1);
        }

        for (var filter : filterList) {
            if (filter.key().equals(key)) {
                filter.include(!isExclude);
                filter.parseValueAndSet(value);
            }
        }
    }

    /**
     * @return Pair left is search text without filters, right is list of filters
     */
    private static Pair<String, List<String>> parseSearch(String searchText) {
        // filters structure is "filter=value", remaining is search
        List<String> filters = new ArrayList<>();
        if (!searchText.contains(DELIMITER)) return new Pair<>(searchText, filters);

        String[] searchArgs = searchText.contains(" ") ? searchText.split(" ") : new String[]{searchText};
        for (var arg : searchArgs) {
            if (arg.contains(DELIMITER)) {
                filters.add(arg);
            }
        }

        String search = Arrays.stream(searchText.split(" "))
                .filter(arg -> !arg.contains(DELIMITER))
                .reduce((accumulator, arg) -> accumulator + " " + arg)
                .orElse("");

        return new Pair<>(search, filters);
    }

    public static String removeFilters(String searchText) {
        return parseSearch(searchText).getA();
    }

    public String key() {
        return this.key;
    }

    public Optional<String> serialize() {
        if (this.value == null) return Optional.empty();

        return Optional.of( (this.isIncluded ? "" : EXCLUDE_MARKER) + this.key + DELIMITER + this.serializeValue());
    }

    public String serializeValue() {
        return this.value == null ? "" : this.value.toString();
    }

    /**
     * @param text Text only contains the value and not key or delimiter
     */
    protected abstract Optional<T> parseValue(String text);

    public abstract TextBoxComponent toInputComponent();

    /**
     * @param text Text only contains the value and not key or delimiter
     */
    protected void parseValueAndSet(String text) {
        this.value = this.parseValue(text).orElse(null);
    }

    public void reset() {
        this.value = null;
        this.isIncluded = true;
    }

    public Optional<T> value() {
        return Optional.ofNullable(this.value);
    }

    public AbstractTextFilter<T, O> value(@Nullable T value) {
        this.value = value;
        return this;
    }

    public boolean isIncluded() {
        return this.isIncluded;
    }

    public void include(boolean value) {
        this.isIncluded = value;
    }

    public boolean matches(O object) {
        if (this.value == null) return true; // filter is not set, skip filter

        return this.predicate.test(this.value, object) == this.isIncluded;
    }

    public static <O> boolean matchesAll(Collection<AbstractTextFilter<?, O>> filters, O object) {
        for (var filter : filters) {
            if (!filter.matches(object)) return false;
        }
        return true;
    }
}
