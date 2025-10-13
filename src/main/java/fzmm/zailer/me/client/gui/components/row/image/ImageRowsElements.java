package fzmm.zailer.me.client.gui.components.row.image;

import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ImageMode;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public record ImageRowsElements(ImageButtonComponent imageButton,
                                SuggestionTextBox valueField,
                                AtomicReference<ImageMode> mode,
                                HashMap<ImageMode, EButtonComponent>  imageModeButtons) {
}
