package fzmm.zailer.me.client.gui.utils.memento;

public interface IMemento {

    IMementoObject createMemento();

    void restoreMemento(IMementoObject mementoObject);
}
