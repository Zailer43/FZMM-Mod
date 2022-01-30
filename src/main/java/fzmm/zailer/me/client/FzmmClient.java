package fzmm.zailer.me.client;

import fi.dy.masa.malilib.event.InitializationHandler;
import fzmm.zailer.me.FzmmInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FzmmClient implements ClientModInitializer {

    public final static String MOD_ID = "fzmm";

    @Override
    public void onInitializeClient() {
        InitializationHandler.getInstance().registerInitializationHandler(new FzmmInitializer());
    }
}