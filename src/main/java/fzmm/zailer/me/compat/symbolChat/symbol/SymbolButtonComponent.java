package fzmm.zailer.me.compat.symbolChat.symbol;

import fzmm.zailer.me.client.FzmmClient;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.lang.reflect.Method;
import java.util.Optional;

public class SymbolButtonComponent {

    public static Optional<ButtonComponent> of(AbstractParentElement customSymbolSelectionPanel, Object symbolButtonWidget, TextFieldWidget textFieldWidget) {
        if (!FzmmClient.SYMBOL_CHAT_PRESENT)
            return Optional.empty();

        net.replaceitem.symbolchat.gui.widget.symbolButton.SymbolButtonWidget symbolButton = (net.replaceitem.symbolchat.gui.widget.symbolButton.SymbolButtonWidget) symbolButtonWidget;

        return Optional.of((ButtonComponent) Components.button(symbolButton.getMessage(), buttonComponent -> {
                    boolean textFieldAlreadyAssigned = setActiveTextField(customSymbolSelectionPanel, textFieldWidget);
                    boolean visible = isVisible(customSymbolSelectionPanel);

                    // Opens the gui if it is closed and only closes it if you click the same button with which you opened it,
                    // otherwise it changes the text field where it writes
                    if (!visible || textFieldAlreadyAssigned)
                        symbolButton.onClick(0, 0);

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
    
    public static boolean setActiveTextField(Object customSymbolSelectionPanel, TextFieldWidget textFieldWidget) {
        try {
            Method setActiveTextField = customSymbolSelectionPanel.getClass().getMethod("setActiveTextField", TextFieldWidget.class);
            return (boolean) setActiveTextField.invoke(customSymbolSelectionPanel, textFieldWidget);
        } catch (Exception e) {
            return false;
        }
    }

}
