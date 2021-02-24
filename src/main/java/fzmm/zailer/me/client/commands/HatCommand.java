package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class HatCommand {

    public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
        return ArgumentBuilders.literal("hat")
                .executes(ctx -> {
                    setHat();

                    return 1;
                });
        }


    public static void setHat() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        ItemStack stack = mc.player.inventory.getMainHandStack();
        mc.player.equipStack(EquipmentSlot.HEAD, stack);
        
    }

}