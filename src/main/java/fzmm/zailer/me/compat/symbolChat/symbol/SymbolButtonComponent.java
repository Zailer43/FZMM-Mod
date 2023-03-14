package fzmm.zailer.me.compat.symbolChat.symbol;

import fzmm.zailer.me.compat.CompatMods;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class SymbolButtonComponent {

    public static Optional<ButtonComponent> of(CustomSymbolSelectionPanel customSymbolSelectionPanel, int x, int y, int width, int height, TextFieldWidget textFieldWidget) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT)
            return Optional.empty();


        net.replaceitem.symbolchat.gui.widget.symbolButton.SymbolButtonWidget symbolButtonWidget =
                new net.replaceitem.symbolchat.gui.widget.symbolButton.OpenSymbolPanelButtonWidget(x, y, width, height, (net.replaceitem.symbolchat.gui.SymbolSelectionPanel) customSymbolSelectionPanel.parent());

        return Optional.of((ButtonComponent) Components.button(symbolButtonWidget.getMessage(), buttonComponent -> {
                    boolean textFieldAlreadyAssigned = setActiveTextField(customSymbolSelectionPanel, textFieldWidget);
                    boolean visible = isVisible(customSymbolSelectionPanel.parent());

                    // Opens the gui if it is closed and only closes it if you click the same button with which you opened it,
                    // otherwise it changes the text field where it writes
                    if (!visible || textFieldAlreadyAssigned)
                        symbolButtonWidget.onClick(0, 0);

//                    Screen screen = MinecraftClient.getInstance().currentScreen;
//                    if (!(screen instanceof BaseFzmmScreen baseFzmmScreen))
//                        return;
//
//                    Optional<FontSelectionDropDownComponent> fontSelectionDropDownComponent = baseFzmmScreen.getFontSelectionDropDown();
//                    if (fontSelectionDropDownComponent.isPresent() && fontSelectionDropDownComponent.get().isVisible())
//                        fontSelectionDropDownComponent.get().setVisible(false);

                }).sizing(Sizing.fixed(20), Sizing.fixed(20))
        );
    }

    public static boolean isVisible(Object symbolSelectionPanel) {
        return ((net.replaceitem.symbolchat.gui.SymbolSelectionPanel) symbolSelectionPanel).visible;
    }

    public static void setVisible(Object symbolSelectionPanel, boolean value) {
        ((net.replaceitem.symbolchat.gui.SymbolSelectionPanel) symbolSelectionPanel).visible = value;
    }
    
    public static boolean setActiveTextField(CustomSymbolSelectionPanel customSymbolSelectionPanel, TextFieldWidget textFieldWidget) {
        AtomicReference<TextFieldWidget> activeTextFieldReference = customSymbolSelectionPanel.activeTextFieldReference();
        if (activeTextFieldReference.get() == textFieldWidget)
            return true;

        activeTextFieldReference.set(textFieldWidget);
        return false;
    }

}
