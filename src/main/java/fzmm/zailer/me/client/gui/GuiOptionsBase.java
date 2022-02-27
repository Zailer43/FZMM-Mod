package fzmm.zailer.me.client.gui;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fzmm.zailer.me.client.gui.enums.Buttons;
import fzmm.zailer.me.client.gui.interfaces.IScreenTab;
import fzmm.zailer.me.client.gui.interfaces.ITabListener;
import fzmm.zailer.me.client.gui.widget.WidgetListOptions;
import fzmm.zailer.me.client.gui.widget.WidgetOption;
import fzmm.zailer.me.client.gui.wrapper.OptionWrapper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

import java.util.List;

public abstract class GuiOptionsBase extends GuiListBase<OptionWrapper, WidgetOption, WidgetListOptions> {
    protected int configWidth = 204;

    protected GuiOptionsBase(String titleKey, Screen parent) {
        super(10, 50);
        this.setTitle(new TranslatableText(titleKey).getString());
        this.setParent(parent);
    }

    @Override
    protected int getBrowserWidth() {
        return this.width - 20;
    }

    @Override
    protected int getBrowserHeight() {
        return this.height - 80;
    }

    protected int getConfigWidth() {
        return this.configWidth;
    }

    @Override
    public void initGui() {
        super.initGui();

        assert this.client != null;
        this.client.keyboard.setRepeatEvents(true);


        ButtonGeneric backButton = Buttons.BACK.getToLeft(this.width - 30, this.height - 40);
        this.addButton(backButton, new BackButtonActionListener());
    }

    @Override
    protected WidgetListOptions createListWidget(int listX, int listY) {
        return new WidgetListOptions(listX, listY, this.getBrowserWidth(), this.getBrowserHeight(), this.getConfigWidth(), this);
    }

    public abstract List<OptionWrapper> getOptions();

    public class BackButtonActionListener implements IButtonActionListener {

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            GuiBase.openGui(GuiOptionsBase.this.getParent());
        }
    }

    public abstract boolean isTab(IScreenTab tab);

    public void createTabs(IScreenTab[] tabList, ITabListener tabListener) {
        int x = ScreenConstants.DEFAULT_TAB_X;
        GuiOptionsBase gui = tabListener.getParent();

        for (IScreenTab tab :  tabList) {
            ButtonGeneric button = new ButtonGeneric(x, ScreenConstants.DEFAULT_TAB_Y, -1, ScreenConstants.NORMAL_BUTTON_HEIGHT, tab.getDisplayName());
            button.setEnabled(!gui.isTab(tab));
            gui.addButton(button, tabListener.of(tab));

            x += button.getWidth() + 2;
        }
    }

    public void reload() {
        this.reCreateListWidget();
        WidgetListOptions listOptions = this.getListWidget();
        assert listOptions != null;
        listOptions.resetScrollbarPosition();
        this.initGui();
    }

}
