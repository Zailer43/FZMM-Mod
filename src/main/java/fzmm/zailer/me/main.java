package fzmm.zailer.me;

import fzmm.zailer.me.client.TextObfuscated;
import fzmm.zailer.me.config.FzmmConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class main implements ModInitializer {

    @Override
    public void onInitialize() {
        FzmmConfig.init();
        TextObfuscated.init();
    }

}
