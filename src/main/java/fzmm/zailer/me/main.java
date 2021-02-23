package fzmm.zailer.me;

import fzmm.zailer.me.config.FzmmConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class main implements ModInitializer {

    public static GameRules.Key<GameRules.BooleanRule> PROJECTILE_EXPLODE = GameRuleRegistry.register(
            "ProjectileExplode",
            GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(false)
    );

    @Override
    public void onInitialize() {
        FzmmConfig.init();
    }

}
