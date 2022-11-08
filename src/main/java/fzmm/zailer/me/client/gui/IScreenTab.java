package fzmm.zailer.me.client.gui;


import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;

public interface IScreenTab {

    String getId();

    Component[] getComponents(BaseFzmmScreen parent);

    void setupComponents(BaseFzmmScreen parent, FlowLayout rootComponent);
}
