package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;


public class NbtCommand {
    public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
        return ArgumentBuilders.literal("nbt").executes(source -> {
                MinecraftClient mc = MinecraftClient.getInstance();

            if (mc.player == null) return 1;
            ItemStack stack = mc.player.inventory.getMainHandStack();

            if (stack.getTag() == null) return 1;
            LiteralText nbt = new LiteralText(stack.getTag().toString());

            mc.inGameHud.addChatMessage(MessageType.SYSTEM, nbt, mc.player.getUuid());

            System.out.println(stack.getTag());
            return 1;
        });
    }
}