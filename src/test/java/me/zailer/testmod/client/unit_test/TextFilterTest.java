package me.zailer.testmod.client.unit_test;

import fzmm.zailer.me.client.gui.utils.text_filter.AbstractTextFilter;
import fzmm.zailer.me.client.gui.utils.text_filter.TextFilterDate;
import fzmm.zailer.me.client.gui.utils.text_filter.TextFilterInteger;
import fzmm.zailer.me.client.gui.utils.text_filter.TextFilterString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static fzmm.zailer.me.client.gui.utils.text_filter.AbstractTextFilter.DELIMITER;
import static fzmm.zailer.me.client.gui.utils.text_filter.AbstractTextFilter.EXCLUDE_MARKER;

public class TextFilterTest {

    private <T, O> void assertEquals(AbstractTextFilter<T, O> filter, String searchText, List<O> values, List<O> expectedResult) {
        String message = String.format("Search text: '%s' filter id: '%s' included: %s", searchText, filter.key(), filter.isIncluded());
        AbstractTextFilter.updateFilters(searchText, List.of(filter));

        List<O> filtered = new ArrayList<>();

        for (var value : values) {
            if (filter.matches(value)) {
                filtered.add(value);
            }
        }

        Assertions.assertEquals(expectedResult, filtered, message);
    }

    @Test
    public void textFilterIntegerEquals() {
        var filter = new TextFilterInteger<Integer>("id", Integer::equals);

        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "123"), List.of(123, 321), List.of(123));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "-500"), List.of(123, 321, -500), List.of(-500));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "0"), List.of(0, 321, -500), List.of(0));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "404"), List.of(0, 321, -500), List.of());

        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "123"), List.of(123, 321, -500), List.of(321, -500));
        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "-500"), List.of(123, 321, -500, -10), List.of(123, 321, -10));
        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "-500"), List.of(123, 321, -500, -10), List.of(123, 321, -10));
        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "-404"), List.of(-404), List.of());
    }

    @Test
    public void textFilterIntegerGreater() {
        var filter = new TextFilterInteger<Integer>("id", (i, i2) -> i < i2);

        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "123"), List.of(123, 321), List.of(321));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "123"), List.of(123, 124, 321, 300, -100, -124), List.of(124, 321, 300));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "-20"), List.of(-80, -5, 123, 500), List.of(-5, 123, 500));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "999"), List.of(-80, -5, 123, 500), List.of());

        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "50"), List.of(-80, -5, 50, 123, 500), List.of(-80, -5, 50));
        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "-20"), List.of(-80, -5, 50, 123, 500), List.of(-80));
        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "-999"), List.of(-80, -5, 50, 123, 500), List.of());
    }

    @Test
    public void textFilterStringEquals() {
        var filter = new TextFilterString<String>("vivimos_en_un_test", String::equals);

        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "test"), List.of("test", "abc"), List.of("test"));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "test"), List.of("test", "TEST"), List.of("test"));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "TEST"), List.of("test", "TEST"), List.of("TEST"));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "hello world"), List.of("test", "hello", "world", "hello world"), List.of("hello"));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "123asd"), List.of("test", "hello", "world", "hello world"), List.of());

        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "hello world"),
                List.of("test", "hello", "world", "hello world"), List.of("test", "world", "hello world")
        );
        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "testing"), List.of("test", "hello", "testing"), List.of("test", "hello"));
        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "HI"), List.of("test", "hello", "testing"), List.of("test", "hello", "testing"));
        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "HI"), List.of("HI"), List.of());
    }

    @Test
    public void textFilterDateEquals() {
        var filter = new TextFilterDate<LocalDate>("date", LocalDate::equals, "yyyy-M-d");

        LocalDate date2025_1_1 = LocalDate.of(2025, 1, 1);
        LocalDate date2025_6_1 = LocalDate.of(2025, 6, 1);
        LocalDate date2025_12_1 = LocalDate.of(2025, 12, 1);
        LocalDate date2025_12_31 = LocalDate.of(2025, 12, 31);
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "2025-01-01"), List.of(date2025_1_1, date2025_6_1), List.of(date2025_1_1));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "2025-12-31"), List.of(date2025_1_1, date2025_6_1, date2025_12_31), List.of(date2025_12_31));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "2025-1-1"), List.of(date2025_1_1), List.of(date2025_1_1));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "2025-12-1"), List.of(date2025_12_1), List.of(date2025_12_1));
        this.assertEquals(filter, String.format("%s%s%s", filter.key(), DELIMITER, "2025-1-1"), List.of(date2025_12_31), List.of());

        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "2025-01-01"), List.of(date2025_1_1, date2025_6_1), List.of(date2025_6_1));
        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "2025-1-1"), List.of(date2025_1_1, date2025_12_31), List.of(date2025_12_31));
        this.assertEquals(filter, String.format("%s%s%s%s", EXCLUDE_MARKER, filter.key(), DELIMITER, "2025-12-31"), List.of(date2025_12_31), List.of());
    }
}
