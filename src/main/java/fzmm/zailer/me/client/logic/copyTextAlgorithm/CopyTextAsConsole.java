package fzmm.zailer.me.client.logic.copyTextAlgorithm;

public class CopyTextAsConsole extends CopyTextAsChatLegacy {
    @Override
    public String getId() {
        return "console";
    }

    @Override
    public String colorCharacter() {
        return "§";
    }
}
