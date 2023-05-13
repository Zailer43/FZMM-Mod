package fzmm.zailer.me.client.gui.components.row.image;

import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import io.wispforest.owo.config.ui.component.ConfigTextBox;

@SuppressWarnings("UnstableApiUsage")
public record ImageRowsElements(ImageButtonComponent imageButton, ConfigTextBox valueField, EnumWidget mode, SuggestionTextBox suggestionTextBox) {
}
