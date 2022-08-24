package fzmm.zailer.me.client.gui;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fzmm.zailer.me.client.gui.enums.Buttons;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class AbstractFzmmScreen extends GuiBase {

    public AbstractFzmmScreen(String titleKey, Screen parent) {
        super();
        this.setParent(parent);
        this.setTitle(Text.translatable(titleKey).getString());
    }

    @Override
    public void initGui() {
        super.initGui();

        this.addButton(Buttons.BACK.getToLeft(this.width - 30, this.height - 40), new ButtonActionListener());
    }

    protected ButtonGeneric createGenericButton(int x, int y, Buttons button) {
        ButtonGeneric buttonGeneric = button.get(x, y, ScreenConstants.NORMAL_BUTTON_WIDTH);
        this.addButton(buttonGeneric, this.getActionListener(button));
        return buttonGeneric;
    }

    protected abstract IButtonActionListener getActionListener(Buttons button);

    private class ButtonActionListener implements IButtonActionListener {

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            GuiBase.openGui(AbstractFzmmScreen.this.getParent());
        }
    }
}
