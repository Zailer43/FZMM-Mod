package fzmm.zailer.me.client.logic.copyTextAlgorithm;

public class CopyTextAsMOTD extends CopyTextAsChatLegacy {
    @Override
    public String getId() {
        return "motd";
    }

    @Override
    public String colorCharacter() {
        return "\\\\u00a7";
    }
}
