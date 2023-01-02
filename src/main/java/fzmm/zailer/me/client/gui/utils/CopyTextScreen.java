package fzmm.zailer.me.client.gui.utils;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.logic.copyTextAlgorithm.CopyText;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class CopyTextScreen extends BaseFzmmScreen {
    private static final String COPY_BUTTONS_LIST_ID = "copy-buttons-list";
    private final Text textToCopy;

    public CopyTextScreen(@Nullable Screen parent, Text textToCopy) {
        super("copy_text", "copyText", parent);
        this.textToCopy = textToCopy;
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        FlowLayout flowLayout = rootComponent.childById(FlowLayout.class, COPY_BUTTONS_LIST_ID);
        if (flowLayout == null)
            return;

        for (var algorithm : CopyText.getAlgorithms()) {
            ButtonRow.setup(rootComponent, ButtonRow.getButtonId(algorithm.getId()), true, buttonComponent -> algorithm.copy(this.textToCopy));
        }
    }
}
