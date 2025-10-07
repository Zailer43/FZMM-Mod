package fzmm.zailer.me.client.logic.history;

import fzmm.zailer.me.client.FzmmClient;

import java.io.*;

public interface IMemento {

    default byte[] backup() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(baos);
            this.backup(output);
            output.close();
            return baos.toByteArray();
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[IMemento] Failed to backup", e);
            return new byte[0];
        }
    }

    void backup(ObjectOutputStream output) throws IOException;

    default void restore(byte[] state) {
        try {
            ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(state));
            this.restore(input);
            input.close();
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[IMemento] Failed to restore", e);
        }
    }

    void restore(ObjectInputStream input) throws IOException, ClassNotFoundException;
}
