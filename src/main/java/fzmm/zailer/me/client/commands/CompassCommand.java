package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class CompassCommand {
    public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
        return ArgumentBuilders.literal("compass").executes(
                ctx -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    assert mc.player != null;

                    setCompass(Math.round(mc.player.getX()), Math.round(mc.player.getZ()));
                    return 1;
                }
        ).then(ArgumentBuilders.argument("x", LongArgumentType.longArg()).executes(ctx -> {
            long x = ctx.getArgument("x", long.class);

            setCompass(x, x);
            return 1;
        }).then(ArgumentBuilders.argument("z", LongArgumentType.longArg()).executes(ctx -> {
            long x = ctx.getArgument("x", long.class);
            long z = ctx.getArgument("z", long.class);

            setCompass(x, z);
            return 1;
        })));
    }

    public static void setCompass(long x, long z) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;
        assert mc.world != null;

        mc.world.setSpawnPos(new BlockPos(x, 0, z), 0.0f);

    }
}
