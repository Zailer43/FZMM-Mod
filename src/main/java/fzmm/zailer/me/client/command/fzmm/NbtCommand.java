package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.TagsConstant;
import fzmm.zailer.me.utils.TextUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NbtCommand implements ISubCommand {
    @Override
    public String alias() {
        return "nbt";
    }

    @Override
    public String syntax() {
        return "nbt";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandBuildContext registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.executes(ctx -> {
            this.showNbt(ctx);
            return 1;
        }).build();
    }

    private void showNbt(CommandContext<FabricClientCommandSource> ctx) {
        Minecraft client = Minecraft.getInstance();
        assert client.player != null;
        ItemStack stack = client.player.getInventory().getSelectedItem();

        Optional<Tag> stackNbtOptional = ItemUtils.encodeToNbt(stack).result();
        // is modified or has NBT
        if (stack.getComponentsPatch().isEmpty() ||
                stackNbtOptional.isEmpty() ||
                !(stackNbtOptional.get() instanceof CompoundTag nbt) ||
                !nbt.contains(TagsConstant.ENCODE_STACK_COMPONENTS)) {

            ctx.getSource().sendError(Component.translatable("commands.fzmm.item.withoutNbt"));
            return;
        }

        final int MAX_HOVER_LENGTH = 15000;
        String nbtString = toFormatedComponent(nbt.getCompoundOrEmpty(TagsConstant.ENCODE_STACK_COMPONENTS), false).getString();
        String nbtStringHover = nbtString;
        nbtString = TextUtils.removeUnpairedMultibyte(nbtString);
        int nbtLength = nbtString.length();

        // if the hover text is too long it gives a lot of lag with cursor over it (and doesn't fit on the screen)
        if (nbtLength > MAX_HOVER_LENGTH) {
            nbtStringHover = "..." + nbtStringHover.substring(nbtLength - MAX_HOVER_LENGTH, nbtLength);
        }

        Component length = Component.literal(String.valueOf(nbtLength))
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));
        MutableComponent lengthMessage = Component.translatable("commands.fzmm.nbt.length", length)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR));

        MutableComponent nbtText = this.nbtToText(nbt, client, nbtString);
        MutableComponent message = this.nbtChatMessageOf(stack, nbtText, nbtString, nbtStringHover);
        client.gui.hud.getChat().addClientSystemMessage(message.append("\n").append(lengthMessage));
    }

    private MutableComponent nbtToText(CompoundTag nbt, Minecraft client, String nbtString) {
        final int MAX_CHAT_LINES = 90; // vanilla chat lines = 100
        // check if the message length fits within 90% of the maximum chat lines in vanilla
        // in order to avoid writing a message too long which could cause crash with mods
        // that increase the limit beyond vanilla (and lag spike in vanilla)
        //
        // note: the final result could be more than 90% due to formatting adding spaces.
        if (client.font.width(nbtString) > ChatComponent.getWidth(client.options.chatWidth().get()) * MAX_CHAT_LINES) {
            String message = String.format("[%s]", Component.translatable("commands.fzmm.nbt.tooLong").getString());
            return Component.literal(message).setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));
        } else {
            return toFormatedComponent(nbt.getCompoundOrEmpty(TagsConstant.ENCODE_STACK_COMPONENTS), true);
        }
    }

    private MutableComponent nbtChatMessageOf(ItemStack stack, MutableComponent nbtMessage, String nbtString, String nbtStringHover) {
        Component clickToCopyMessage = Component.literal(" (").append(Component.translatable("commands.fzmm.nbt.click")).append(")")
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));

        return Component.literal(stack.getItem().toString()).setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR))
                .append(nbtMessage.copy().append(clickToCopyMessage)
                        .setStyle(nbtMessage.getStyle()
                                .withClickEvent(new ClickEvent.CopyToClipboard(nbtString))
                                .withHoverEvent(new HoverEvent.ShowText(Component.literal(nbtStringHover)))
                        )
                );
    }

    public static MutableComponent toFormatedComponent(CompoundTag nbt, boolean prettyPrint) {
        MutableComponent result = Component.literal("[");
        List<Component> componentsText = new ArrayList<>(nbt.keySet().size());

        for (var key : nbt.keySet()) {
            MutableComponent text = Component.empty();
            Tag tag = nbt.get(key);

            if (tag == null) {
                tag = new CompoundTag();
            }

            text.append(Component.literal(key).setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_AQUA)));
            text.append(Component.literal("="));
            if (prettyPrint) {
                text.append(NbtUtils.toPrettyComponent(tag));
            } else {
                text.append(tag.toString());
            }

            componentsText.add(text);
        }

        for (int i = 0; i != componentsText.size(); i++) {
            result.append(componentsText.get(i));

            if (i != componentsText.size() - 1) {
                result.append(", ");
            }
        }

        return result.append("]");
    }
}
