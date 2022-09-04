package fzmm.zailer.me.client.gui;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IMessageConsumer;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.enums.Buttons;
import fzmm.zailer.me.client.gui.interfaces.IScreenTab;
import fzmm.zailer.me.client.gui.interfaces.ITabListener;
import net.minecraft.client.gui.screen.Screen;

public abstract class GuiOptionsBase extends GuiConfigsBase implements IMessageConsumer {
    protected final String commentBase;
    protected int configWidth = 204;

    protected GuiOptionsBase(String titleKey, Screen parent) {
        super(10, 50, FzmmClient.MOD_ID, null, FzmmClient.MOD_ID + ".gui.title." + titleKey);
        this.setParent(parent);
        this.commentBase =  FzmmClient.MOD_ID + ".gui." + titleKey + ".comment.";
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
        this.addButton(backButton, (button, mouseButton) -> GuiBase.openGui(this.getParent()));
    }

    @Override
    protected WidgetListConfigOptions createListWidget(int listX, int listY) {
        return new WidgetListConfigOptions(listX, listY, this.getBrowserWidth(), this.getBrowserHeight(), this.getConfigWidth(), this.getZOffset(), false, this);
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
        WidgetListConfigOptions listOptions = this.getListWidget();
        assert listOptions != null;
        listOptions.resetScrollbarPosition();
        this.initGui();
    }

}
