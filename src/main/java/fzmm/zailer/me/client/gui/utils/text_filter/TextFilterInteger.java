package fzmm.zailer.me.client.gui.utils.text_filter;

import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.TextBoxComponent;

import java.util.Optional;
import java.util.function.BiPredicate;

public class TextFilterInteger<O> extends AbstractTextFilter<Integer, O> {

    public TextFilterInteger(String key, BiPredicate<Integer, O> predicate) {
        super(key, predicate);
    }

    @Override
    protected Optional<Integer> parseValue(String text) {
        try {
            return Optional.of(Integer.parseInt(text));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public TextBoxComponent toInputComponent() {
        ConfigTextBox result = new ConfigTextBox().configureForNumber(Integer.class);
        result.applyPredicate(s -> this.parseValue(s).isPresent());
        return result;
    }
}
