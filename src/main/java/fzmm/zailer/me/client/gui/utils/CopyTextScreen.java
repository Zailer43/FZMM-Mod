package fzmm.zailer.me.client.gui.utils;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.logic.copy_text_algorithm.CopyText;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class CopyTextScreen extends BaseFzmmScreen {
    private final Text textToCopy;

    public CopyTextScreen(@Nullable Screen parent, Text textToCopy) {
        super("utils/copy_text", "copyText", parent);
        this.textToCopy = textToCopy;
    }

    @Override
    protected void setup(EFlowLayout rootComponent) {
        FlowLayout flowLayout = rootComponent.childById(FlowLayout.class, "copy-buttons-list");
        if (flowLayout == null) {
            return;
        }

        for (var algorithm : CopyText.getAlgorithms()) {
            rootComponent.childByIdOrThrow(ButtonComponent.class, algorithm.getId() + "-button")
                    .onPress(buttonComponent -> algorithm.copy(this.textToCopy));
        }
    }
}
