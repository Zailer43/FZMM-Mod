package fzmm.zailer.me.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.DynamicOps;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarScreen;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.*;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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

    public static MutableText disableItalicConfig(MutableText message) {
        Style style = message.getStyle();

        if (FzmmClient.CONFIG.general.disableItalic() && !style.isItalic()) {
            message.setStyle(style.withItalic(false));
        }

        return message;
    }


    public static MutableText disableItalicConfig(String string, boolean useDisableItalicConfig) {
        return disableItalicConfig(Text.literal(string), useDisableItalicConfig);
    }

    public static MutableText disableItalicConfig(MutableText text, boolean useDisableItalicConfig) {
        if (useDisableItalicConfig) {
            return disableItalicConfig(text);
        }
        return text;
    }

    public static int getMaxWidth(Collection<StringVisitable> collection) {
        return getMaxWidth(collection, stringVisitable -> stringVisitable);
    }

    /**
     * @param widthGetter Object is either StringVisitable or OrderedText
     */
    public static <T> int getMaxWidth(Collection<T> collection, Function<T, Object> widthGetter) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int max = 0;

        for (T t : collection) {
            // is an object because a generic object with both interfaces gives this
            // Ambiguous method call. Both getWidth (StringVisitable)
            // in TextRenderer and getWidth (OrderedText) in TextRenderer
            //
            // and 2 methods with polymorphism are not compatible since only one
            // data type within the Function varies, and it detects it as the same method signature
            Object text = widthGetter.apply(t);
            int width;

            if (text instanceof StringVisitable) {
                width = textRenderer.getWidth((StringVisitable) text);
            } else {
                width = textRenderer.getWidth((OrderedText) text);
            }
            max = Math.max(max, width);
        }

        return max;
    }

    public static DyeColor[] getDyeColorsInOrder() {
        DyeColor[] result = new DyeColor[]{
                DyeColor.WHITE,
                DyeColor.LIGHT_GRAY,
                DyeColor.GRAY,
                DyeColor.BLACK,
                DyeColor.BROWN,
                DyeColor.RED,
                DyeColor.ORANGE,
                DyeColor.YELLOW,
                DyeColor.LIME,
                DyeColor.GREEN,
                DyeColor.CYAN,
                DyeColor.LIGHT_BLUE,
                DyeColor.BLUE,
                DyeColor.PURPLE,
                DyeColor.MAGENTA,
                DyeColor.PINK
        };

        return addToArray(result, DyeColor.values());
    }

    public static Formatting[] getFormattingColorsInOrder() {
        Formatting[] result = new Formatting[]{
                Formatting.WHITE,
                Formatting.GRAY,
                Formatting.DARK_GRAY,
                Formatting.BLACK,
                Formatting.DARK_RED,
                Formatting.RED,
                Formatting.GOLD,
                Formatting.YELLOW,
                Formatting.GREEN,
                Formatting.DARK_GREEN,
                Formatting.DARK_AQUA,
                Formatting.AQUA,
                Formatting.BLUE,
                Formatting.DARK_BLUE,
                Formatting.DARK_PURPLE,
                Formatting.LIGHT_PURPLE,
        };

        return addToArray(result, Formatting.values());
    }

    private static <T> T[] addToArray(T[] sortedArray, T[] allArray) {
        int lastIndex = sortedArray.length;
        sortedArray = Arrays.copyOf(sortedArray, allArray.length);

        List<T> notFound = new ArrayList<>(Arrays.asList(allArray));
        notFound.removeAll(Arrays.asList(sortedArray));

        for (var value : notFound) {
            sortedArray[lastIndex++] = value;
        }

        return sortedArray;
    }

    public static DynamicRegistryManager getRegistryManager() {
        assert MinecraftClient.getInstance().player != null;
        return MinecraftClient.getInstance().player.getRegistryManager();
    }

    public static <T> RegistryOps<T> getRegistryOps(DynamicOps<T> registry) {
        return getRegistryManager().getOps(registry);
    }

    public static <T extends Screen & ISnackBarScreen> void setScreen(T screen) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof ISnackBarScreen snackBarScreen) {
            snackBarScreen.setScreen(screen);
        } else {
            client.setScreen(screen);
            SnackBarManager.getInstance().moveToScreen(screen);
        }
    }

    public static CloseableHttpClient getHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(3000)
                .setSocketTimeout(3000)
                .build();

        return HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .disableAutomaticRetries()
                .setDefaultRequestConfig(requestConfig)
                .setUserAgent(FzmmClient.HTTP_USER_AGENT)
                .build();
    }

    /**
     * @param username player's username (capitalization is ignored)
     * @return {@code null} if player is not online, otherwise {@link PlayerListEntry}
     */
    @Nullable
    public static PlayerListEntry getOnlinePlayer(String username) {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) return null;

        List<PlayerListEntry> playerList = networkHandler.getPlayerList().stream()
                .filter(entry -> entry.getProfile().getName().equalsIgnoreCase(username))
                .toList();

        // entry can be null in some cases
        return playerList.isEmpty() ? null : playerList.get(0);
    }

}