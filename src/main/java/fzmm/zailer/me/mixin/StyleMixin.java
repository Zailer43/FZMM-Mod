package fzmm.zailer.me.mixin;

import fzmm.zailer.me.config.FzmmConfig;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Style.class)
public class StyleMixin {

    @Nullable
    public final Boolean obfuscated;

    public StyleMixin(@Nullable Boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    /**
     * @author Zailer43
     */

    @Overwrite
    public boolean isObfuscated() {
        FzmmConfig config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();

        if (config.general.textObfuscated) return false;
        else return this.obfuscated == Boolean.TRUE;
    }
}
