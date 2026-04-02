package fzmm.zailer.me.client.gui.components.extend.component;

import fzmm.zailer.me.client.FzmmClient;
import io.wispforest.owo.ui.component.LabelComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public class ELabelComponent extends LabelComponent {

    public ELabelComponent(Component text) {
        super(text);

        // improves text readability with translucent background
        boolean oldBackground = FzmmClient.CONFIG.guiStyle.oldBackground();
        if (!oldBackground) {
            this.shadow(true);
        }
    }

    private Component applyStyle(Component text) {
//        if (!text.getStyle().isEmpty()) {
//            return text;
//        }
//
////        boolean darkMode = FzmmClient.CONFIG.guiStyle.darkMode();
////        int color = (darkMode ? Color.WHITE : Color.BLACK).argb();
//
//
//        return text.copy().setStyle(text.getStyle().withColor(color));
        return text;
    }

    @Override
    public LabelComponent text(Component text) {
        return super.text(this.applyStyle(text));
    }

    // owo-lib 26.1 workaround
    @Override
    protected @Nullable Style styleAt(int mouseX, int mouseY) {
        Style result = super.styleAt(mouseX, mouseY);
        if (result == null) return Style.EMPTY;
        return result;
    }
}
