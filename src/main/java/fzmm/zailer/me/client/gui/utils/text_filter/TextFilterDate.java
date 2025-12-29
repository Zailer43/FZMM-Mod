package fzmm.zailer.me.client.gui.utils.text_filter;

import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.TextBoxComponent;
import net.minecraft.network.chat.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.BiPredicate;

public class TextFilterDate<O> extends AbstractTextFilter<LocalDate, O> {
    private final String pattern;
    private final DateTimeFormatter formatter;

    public TextFilterDate(String key, BiPredicate<LocalDate, O> predicate, String pattern) {
        super(key, predicate);
        this.pattern = pattern;
        this.formatter = DateTimeFormatter.ofPattern(pattern);
    }

    @Override
    protected Optional<LocalDate> parseValue(String text) {
        try {
            return Optional.of(LocalDate.parse(text, this.formatter));
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public TextBoxComponent toInputComponent() {
        ConfigTextBox result = new ConfigTextBox();
        result.applyPredicate(s -> this.parseValue(s).isPresent());
        result.setMessage(Component.literal(this.pattern));
        return result;
    }
}
