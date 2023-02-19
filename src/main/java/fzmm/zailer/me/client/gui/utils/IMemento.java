package fzmm.zailer.me.client.gui.utils;

public interface IMemento {

    IMementoObject createMemento();

    void restoreMemento(IMementoObject mementoObject);
}
