package fzmm.zailer.me.utils;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.FzmmHistory;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FzmmUtils {

    public static final SuggestionProvider<FabricClientCommandSource> SUGGESTION_PLAYER = (context, builder) -> {
        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
        String playerInput = builder.getRemainingLowerCase();
        if (clientPlayer != null) {
            List<String> playerNamesList = clientPlayer.networkHandler.getPlayerList().stream()
                    .map(PlayerListEntry::getProfile)
                    .map(GameProfile::getName)
                    .toList();

            for (String playerName : playerNamesList) {
                if (playerName.toLowerCase().contains(playerInput))
                    builder.suggest(playerName);
            }
        }

        return CompletableFuture.completedFuture(builder.build());

    };

    public static void giveItem(ItemStack stack) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        if (getLength(stack) > 1950000) {
            mc.inGameHud.getChatHud().addMessage(Text.translatable("fzmm.giveItem.exceedLimit").setStyle(Style.EMPTY.withColor(Formatting.RED)));
            return;
        }

        if (Items.PLAYER_HEAD == stack.getItem())
            FzmmHistory.addGeneratedHeads(stack);
        else
            FzmmHistory.addGeneratedItems(stack);

        if (FzmmClient.CONFIG.general.giveClientSide()) {
            mc.player.equipStack(EquipmentSlot.MAINHAND, stack);
        } else {
            assert mc.interactionManager != null;
            PlayerInventory playerInventory = mc.player.getInventory();

            playerInventory.addPickBlock(stack);
            updateHand(stack);
        }
    }

    public static void updateHand(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.interactionManager != null;
        assert client.player != null;

        PlayerInventory playerInventory = client.player.getInventory();
        client.interactionManager.clickCreativeStack(stack, PlayerInventory.MAIN_SIZE + playerInventory.selectedSlot);
    }

    public static Text disableItalicConfig(Text message) {
        Style style = message.getStyle();

        if (FzmmClient.CONFIG.general.disableItalic() && !style.isItalic()) {
            ((MutableText) message).setStyle(style.withItalic(false));
        }

        return message;
    }

    public static String getLengthInKB(ItemStack stack) {
        return new DecimalFormat("#,##0.0").format(getLength(stack) / 1024f) + "KB";
    }

    public static long getLength(ItemStack stack) {
        ByteCountDataOutput byteCountDataOutput = ByteCountDataOutput.getInstance();

        try {
            stack.writeNbt(new NbtCompound()).write(byteCountDataOutput);
        } catch (Exception ignored) {
            return 0;
        }

        long count = byteCountDataOutput.getCount();
        byteCountDataOutput.reset();
        return count;
    }

    public static NbtString toNbtString(String string, boolean useDisableItalicConfig) {
        Text text = Text.of(string);
        return toNbtString(text, useDisableItalicConfig);
    }

    public static NbtString toNbtString(Text text, boolean useDisableItalicConfig) {
        if (useDisableItalicConfig)
            disableItalicConfig(text);
        return NbtString.of(Text.Serializer.toJson(text));
    }

    public static String getPlayerUuid(String name) throws IOException, JsonIOException {
        try (var httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://api.mojang.com/users/profiles/minecraft/" + name);

            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();
            if (((response.getStatusLine().getStatusCode() / 100) != 2) || resEntity == null)
                return "";

            InputStream inputStream = resEntity.getContent();
            JsonObject obj = (JsonObject) JsonParser.parseReader(new InputStreamReader(inputStream));
            return obj.get("id").getAsString();
        }
    }

    public static Item getItem(String value) {
        return Registries.ITEM.getOrEmpty(new Identifier(value)).orElse(Items.STONE);
    }

    public static boolean isCreative() {
        assert MinecraftClient.getInstance().player != null;
        return MinecraftClient.getInstance().player.isCreative() || FzmmClient.CONFIG.general.giveClientSide();
    }

    /**
     * Splits the characters of a message correctly including multibyte characters correctly
     */
    public static List<String> splitMessage(String message) {
        return Arrays.asList(message.split("(?s)(?<=.)"));
    }
}