package fzmm.zailer.me.compat;

import net.fabricmc.loader.api.FabricLoader;

public class CompatMods {
    public static boolean SYMBOL_CHAT_PRESENT = FabricLoader.getInstance().isModLoaded("symbol-chat");
    public static boolean PLACEHOLDER_API_PRESENT = FabricLoader.getInstance().isModLoaded("placeholder-api");
}
