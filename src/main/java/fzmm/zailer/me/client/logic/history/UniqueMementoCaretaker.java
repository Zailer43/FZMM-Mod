package fzmm.zailer.me.client.logic.history;

import java.util.HashMap;

public class UniqueMementoCaretaker {
    private final HashMap<Class<? extends IMemento>, byte[]> mementos = new HashMap<>();

    public <T extends IMemento> void backup(T memento) {
        this.mementos.put(memento.getClass(), memento.backup());
    }

    public <T extends IMemento> void restore(T memento) {
        byte[] state = this.mementos.get(memento.getClass());
        if (state != null) {
            memento.restore(state);
        }
    }
}
