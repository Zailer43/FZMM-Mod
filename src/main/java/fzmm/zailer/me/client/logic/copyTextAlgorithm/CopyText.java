package fzmm.zailer.me.client.logic.copyTextAlgorithm;

import fzmm.zailer.me.client.logic.copyTextAlgorithm.algorithms.*;

public class CopyText {
    private static final AbstractCopyTextAlgorithm[] algorithms;

    static {
        algorithms = new AbstractCopyTextAlgorithm[] {
                new CopyTextAsJson(),
                new CopyTextAsChatDefault(),
                new CopyTextAsChatLegacy(),
                new CopyTextAsConsole(),
                new CopyTextAsMOTD(),
                new CopyTextAsXml(),
                new CopyTextAsBBCode(),
                new CopyTextAsString()
        };
    }

    public static AbstractCopyTextAlgorithm[] getAlgorithms() {
        return algorithms;
    }
}
