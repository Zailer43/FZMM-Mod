package fzmm.zailer.me.client.gui.components.row.image;

import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.ButtonComponent;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("UnstableApiUsage")
public record ImageRowsElements(ImageButtonComponent imageButton,
                                ConfigTextBox valueField,
                                AtomicReference<ImageMode> mode,
                                HashMap<ImageMode, ButtonComponent>  imageModeButtons,
                                SuggestionTextBox suggestionTextBox) {
}
