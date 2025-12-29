package me.zailer.testmod.client.unit_test;

import fzmm.zailer.me.client.gui.utils.text_filter.AbstractTextFilter;
import fzmm.zailer.me.client.gui.utils.text_filter.TextFilterDate;
import fzmm.zailer.me.client.gui.utils.text_filter.TextFilterInteger;
import fzmm.zailer.me.client.gui.utils.text_filter.TextFilterString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static fzmm.zailer.me.client.gui.utils.text_filter.AbstractTextFilter.DELIMITER;
import static fzmm.zailer.me.client.gui.utils.text_filter.AbstractTextFilter.EXCLUDE_MARKER;

public class TextFilterValueTest {

    @Test
    public void searchTextWithoutFilters() {
        Assertions.assertEquals("tnt", AbstractTextFilter.removeFilters("tnt"));
        Assertions.assertEquals("apple", AbstractTextFilter.removeFilters("id_after" + DELIMITER + "30 apple"));
        Assertions.assertEquals("stone", AbstractTextFilter.removeFilters("stone date_after" + DELIMITER + "2022-01-01"));
        Assertions.assertEquals("stone block", AbstractTextFilter.removeFilters("stone date_after" + DELIMITER + "2022-01-01 block"));
        Assertions.assertEquals("", AbstractTextFilter.removeFilters(""));
        Assertions.assertEquals("", AbstractTextFilter.removeFilters("id_after" + DELIMITER + "30"));
        Assertions.assertEquals("", AbstractTextFilter.removeFilters("id_after" + DELIMITER + "30 date_after" + DELIMITER + "2022-01-01"));
        Assertions.assertEquals("end", AbstractTextFilter.removeFilters("id_after" + DELIMITER + "30 date_after" + DELIMITER + "2022-01-01 end"));
    }

    @SafeVarargs
    private <T> void basic(AbstractTextFilter<T, T> filter, T... values) {
        Assertions.assertTrue(values.length > 2);
        T invalidValue = values[values.length - 1];

        for (int i = 0; i < values.length - 1; i++) {
            T value = values[i];
            String valueStr = value.toString();
            this.basicSuccess(filter, value, valueStr, invalidValue);
            this.basicFail(filter, invalidValue, valueStr);

            String excludeFilterSearch = EXCLUDE_MARKER + filter.key() + DELIMITER + valueStr;
            this.basicExcludeSuccess(filter, excludeFilterSearch, value);
            this.basicExcludeFail(filter, excludeFilterSearch);
        }

    }

    private <T> void basicSuccess(AbstractTextFilter<T, T> filter, T value, String valueStr, T invalidValue) {
        String filterSearch = filter.key() + DELIMITER + valueStr;
        this.assertEquals(filter, value, filterSearch);
        this.assertEquals(filter, value, filter.key().toUpperCase() + DELIMITER + valueStr);
        this.assertEquals(filter, value, String.format("%s search", filterSearch));
        this.assertEquals(filter, value, String.format("search %s", filterSearch));
        this.assertEquals(filter, value, String.format("search %s search", filterSearch));
        this.assertEquals(filter, value, String.format("%s other_value%s%s", filterSearch, DELIMITER, invalidValue));
    }

    private <T> void basicFail(AbstractTextFilter<T, T> filter, T invalidValue, String valueStr) {
        this.assertEquals(filter, null, valueStr);
        this.assertEquals(filter, null, String.format("fail%s%s%s", filter.key(), DELIMITER, valueStr));
        this.assertEquals(filter, null, String.format("other_value%s%s", DELIMITER, invalidValue));
        this.assertEquals(filter, null, String.format("%s other_value%s%s", valueStr, DELIMITER, valueStr));
    }

    private <T> void basicExcludeSuccess(AbstractTextFilter<T, T> filter, String excludeSearch, T value) {
        this.assertEquals(filter, value, false, String.format("something %s", excludeSearch));
        this.assertEquals(filter, value, false, String.format("%s something", excludeSearch));
        this.assertEquals(filter, value, false, String.format("something %s something", excludeSearch));
    }

    private <T> void basicExcludeFail(AbstractTextFilter<T, T> filter, String excludeSearch) {
        this.assertEquals(filter, null, true, String.format("fail%s", excludeSearch));
        this.assertEquals(filter, null, true, String.format("fail%sfail", excludeSearch));
    }

    private <T, O> void assertEquals(AbstractTextFilter<T, O> filter, O expected, String searchText) {
        this.assertEquals(filter, expected, true, searchText);
    }

    private <T, O> void assertEquals(AbstractTextFilter<T, O> filter, O expectedValue, boolean expectedIncluded, String searchText) {
        String message = String.format("Search text: '%s' filter id: '%s'", searchText, filter.key());
        AbstractTextFilter.updateFilters(searchText, List.of(filter));
        Assertions.assertEquals(expectedValue, filter.value().orElse(null), message);
        Assertions.assertEquals(expectedIncluded, filter.isIncluded(), message);
    }

    @Test
    public void textFilterInteger() {
        var filter = new TextFilterInteger<Integer>("id", Integer::equals);

        this.basic(filter, 123, -123, 0, 99999999);
    }

    @Test
    public void textFilterString() {
        var filter = new TextFilterString<String>("value", String::equals);

        this.basic(filter, "stone", "apple", "cobblestone", "air", "test:stone");

        this.assertEquals(filter, "STONE", "value" + DELIMITER + "STONE");
        this.assertEquals(filter, "COBBLESTONE", "VALUE" + DELIMITER + "COBBLESTONE");
        String url = "http://textures.minecraft.net/texture/9636dee806ba47a2c40e95b57a12f37de6c2e677f2160132a07e24eeffa6";
        this.assertEquals(filter, url, "value" + DELIMITER + url);
    }

    @Test
    public void textFilterDate() {
        var filter = new TextFilterDate<LocalDate>("date", LocalDate::equals, "yyyy-M-d");

        this.basic(filter,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 10, 10),
                LocalDate.of(2025, 12, 31)
        );

        this.assertEquals(filter, LocalDate.of(2025, 1, 1), "date" + DELIMITER + "2025-1-1");
        this.assertEquals(filter, LocalDate.of(2025, 1, 1), "date" + DELIMITER + "2025-01-01");
        this.assertEquals(filter, LocalDate.of(2025, 10, 10), "date" + DELIMITER + "2025-10-10");
        this.assertEquals(filter, LocalDate.of(2025, 12, 31), "date" + DELIMITER + "2025-12-31");

        this.assertEquals(filter, null, "date" + DELIMITER + "2025-11");
        this.assertEquals(filter, null, "date" + DELIMITER + "2025");
        this.assertEquals(filter, null, "date" + DELIMITER + "2025-13-1");
        this.assertEquals(filter, null, "date" + DELIMITER + "2025-1-0");
        this.assertEquals(filter, null, "date" + DELIMITER + "2025-13-32");
        this.assertEquals(filter, null, "date" + DELIMITER + "25-1-1");
    }
}
