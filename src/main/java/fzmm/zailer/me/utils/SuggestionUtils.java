package fzmm.zailer.me.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.wispforest.owo.Owo;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class SuggestionUtils {

    public static final SuggestionProvider<FabricClientCommandSource> PLAYER = (context, builder) -> {
        String input = builder.getRemainingLowerCase();
        var players = filterPlayers(input);

        for (var player : players) {
            builder.suggest(player.name());
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    public static List<GameProfile> filterPlayers(String input) {
        var result = new ArrayList<GameProfile>();
        var player = Minecraft.getInstance().player;
        if (player == null) return result;

        List<GameProfile> profiles;
        if (Owo.DEBUG) {
            profiles = new ArrayList<>();
            for (int i = 0; i != 100; i++) {
                profiles.add(UUIDUtil.createOfflineProfile("Player" + i));
            }
        } else {
            profiles = player.connection.getOnlinePlayers().stream()
                    .map(PlayerInfo::getProfile)
                    .sorted((o1, o2) -> Comparator.<String>naturalOrder().compare(o1.name(), o2.name()))
                    .toList();
        }

        for (var profile : profiles) {
            if (profile.name().toLowerCase(Locale.ROOT).contains(input)) {
                result.add(profile);
            }
        }

        return result;
    }

    public static Component createComponent(String suggestion, String text, int accentColor) {
        var textChars = TextUtils.splitMessage(text);
        if (textChars.isEmpty()) return normalText(suggestion);

        var suggestionChars = TextUtils.splitMessage(suggestion);
        int matchIndex = matchIndexOf(suggestionChars, textChars);
        if (matchIndex < 0) return normalText(suggestion);

        return highlightText(suggestionChars, matchIndex, textChars.size(), accentColor);
    }

    private static int matchIndexOf(List<String> suggestionChars, List<String> textChars) {
        int suggestionSize = suggestionChars.size();
        int textSize = textChars.size();
        if (textSize > suggestionSize) return -1;

        for (int i = 0; i <= suggestionSize - textSize; i++) {
            boolean matches = true;

            for (int j = 0; j != textSize; j++) {
                if (!suggestionChars.get(i + j).equalsIgnoreCase(textChars.get(j))) {
                    matches = false;
                    break;
                }
            }

            if (matches) return i;
        }

        return -1;
    }

    private static Component highlightText(List<String> characters, int matchIndex, int matchLength, int accentColor) {
        int matchEnd = matchIndex + matchLength;

        String preMatch = String.join("", characters.subList(0, matchIndex));
        String match = String.join("", characters.subList(matchIndex, matchEnd));
        String postMatch = String.join("", characters.subList(matchEnd, characters.size()));

        return Component.empty()
                .append(normalText(preMatch))
                .append(Component.literal(match).setStyle(Style.EMPTY.withColor(accentColor)))
                .append(normalText(postMatch));
    }

    private static Component normalText(String text) {
        return Component.empty().append(Component.literal(text).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
    }
}

