package fzmm.zailer.me.client.gui.utils.text_filter;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Sizing;

import java.util.Optional;
import java.util.function.BiPredicate;

public class TextFilterString<O> extends AbstractTextFilter<String, O> {

    public TextFilterString(String key, BiPredicate<String, O> predicate) {
        super(key, predicate);
    }

    @Override
    protected Optional<String> parseValue(String text) {
        return Optional.of(text);
    }

    @Override
    public TextBoxComponent toInputComponent() {
        TextBoxComponent result = Components.textBox(Sizing.fixed(100));
        result.setMaxLength(256);
        return result;
    }
}
