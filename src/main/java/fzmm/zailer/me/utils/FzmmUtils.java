package fzmm.zailer.me.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.DynamicOps;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarScreen;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class FzmmUtils {

    public static final SuggestionProvider<FabricClientCommandSource> SUGGESTION_PLAYER = (context, builder) -> {
        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        String playerInput = builder.getRemainingLowerCase();
        if (clientPlayer != null) {
            List<String> playerNamesList = clientPlayer.connection.getOnlinePlayers().stream()
                    .map(PlayerInfo::getProfile)
                    .map(GameProfile::name)
                    .toList();

            for (String playerName : playerNamesList) {
                if (playerName.toLowerCase(Locale.ROOT).contains(playerInput))
                    builder.suggest(playerName);
            }
        }

        return CompletableFuture.completedFuture(builder.build());

    };

    public static MutableComponent disableItalicConfig(MutableComponent message) {
        Style style = message.getStyle();

        if (FzmmClient.CONFIG.general.disableItalic() && !style.isItalic()) {
            message.setStyle(style.withItalic(false));
        }

        return message;
    }


    public static MutableComponent disableItalicConfig(String string, boolean useDisableItalicConfig) {
        return disableItalicConfig(Component.literal(string), useDisableItalicConfig);
    }

    public static MutableComponent disableItalicConfig(MutableComponent text, boolean useDisableItalicConfig) {
        if (useDisableItalicConfig) {
            return disableItalicConfig(text);
        }
        return text;
    }

    public static int getMaxWidth(Collection<FormattedText> collection) {
        return getMaxWidth(collection, stringVisitable -> stringVisitable);
    }

    /**
     * @param widthGetter Object is either StringVisitable or OrderedText
     */
    public static <T> int getMaxWidth(Collection<T> collection, Function<T, Object> widthGetter) {
        Font textRenderer = Minecraft.getInstance().font;
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

            if (text instanceof FormattedText) {
                width = textRenderer.width((FormattedText) text);
            } else {
                width = textRenderer.width((FormattedCharSequence) text);
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

    public static ChatFormatting[] getFormattingColorsInOrder() {
        ChatFormatting[] result = new ChatFormatting[]{
                ChatFormatting.WHITE,
                ChatFormatting.GRAY,
                ChatFormatting.DARK_GRAY,
                ChatFormatting.BLACK,
                ChatFormatting.DARK_RED,
                ChatFormatting.RED,
                ChatFormatting.GOLD,
                ChatFormatting.YELLOW,
                ChatFormatting.GREEN,
                ChatFormatting.DARK_GREEN,
                ChatFormatting.DARK_AQUA,
                ChatFormatting.AQUA,
                ChatFormatting.BLUE,
                ChatFormatting.DARK_BLUE,
                ChatFormatting.DARK_PURPLE,
                ChatFormatting.LIGHT_PURPLE,
        };

        return addToArray(result, ChatFormatting.values());
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

    public static RegistryAccess getRegistryManager() {
        assert Minecraft.getInstance().player != null;
        return Minecraft.getInstance().player.registryAccess();
    }

    public static <T> RegistryOps<T> getRegistryOps(DynamicOps<T> registry) {
        return getRegistryManager().createSerializationContext(registry);
    }

    public static <T extends Screen & ISnackBarScreen> void setScreen(T screen) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen instanceof ISnackBarScreen snackBarScreen) {
            snackBarScreen.setScreen(screen);
        } else {
            client.setScreen(screen);
            SnackBarManager.getInstance().moveToScreen(screen);
        }
    }

}