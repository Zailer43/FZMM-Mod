package fzmm.zailer.me.client.logic.copyTextAlgorithm;

public class CopyText {
    private static final AbstractCopyTextAlgorithm[] algorithms = new AbstractCopyTextAlgorithm[] {
            new CopyTextAsJson(),
            new CopyTextAsChatDefault(),
            new CopyTextAsChatLegacy(),
            new CopyTextAsConsole(),
            new CopyTextAsMOTD(),
            new CopyTextAsXml(),
            new CopyTextAsBBCode()
    };

    public static AbstractCopyTextAlgorithm[] getAlgorithms() {
        return algorithms;
    }
}
