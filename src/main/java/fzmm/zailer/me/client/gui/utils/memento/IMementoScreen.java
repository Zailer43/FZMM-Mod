package fzmm.zailer.me.client.gui.utils.memento;

import java.util.Optional;

public interface IMementoScreen {

    void setMemento(IMementoObject memento);

    Optional<IMementoObject> getMemento();

    IMementoObject createMemento();

    void restoreMemento(IMementoObject mementoObject);
}
