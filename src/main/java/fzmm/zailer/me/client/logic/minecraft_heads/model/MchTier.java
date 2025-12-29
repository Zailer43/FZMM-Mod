package fzmm.zailer.me.client.logic.minecraft_heads.model;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.minecraft_heads.MinecraftHeadsResources;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class MchTier {
    public static final MchTier[] ALL;
    public static final MchTier INVALID_LICENSE; // in case there is an error or by default if it has not been initiated
    public static final MchTier NO_LICENSE;
    public static final MchTier FREE;
    public static final MchTier BRONZE;
    public static final MchTier SILVER;
    public static final MchTier GOLD;

    // heads API
    @SuppressWarnings("PointlessBitwiseExpression")
    public static final int HEADS_GENERAL_REQUEST = 1 << 0;
    public static final int HEADS_BASIC_DATA = 1 << 1;
    public static final int HEADS_BEST_HEADS = 1 << 2;
    public static final int HEADS_ADD_DATA_FREE = 1 << 3;
    public static final int HEADS_ALL_HEADS = 1 << 4;
    public static final int HEADS_ADD_DATA_TAGS = 1 << 5;
    public static final int HEADS_EARLY_ACCESS = 1 << 6;
    public static final int HEADS_TRANSLATIONS = 1 << 7;

    // filterOption API
    public static final int CATEGORY_GENERAL_REQUEST = 1 << 8;
    public static final int CATEGORY_TRANSLATIONS = 1 << 9;

    // tag API
    public static final int TAG_GENERAL_REQUEST = 1 << 10;
    public static final int TAG_TRANSLATIONS = 1 << 11;

    // collection API
    public static final int COLLECTION_GENERAL_REQUEST = 1 << 12;

    // collection API (other players)
    public static final int COLLECTION_PLAYERS_GENERAL_REQUEST = 1 << 13;

    private final Component message;
    private final int permissions;

    protected MchTier(Component message, int permissions) {
        this.message = message;
        this.permissions = permissions;
    }

    public Component message() {
        return this.message;
    }

    public boolean hasPermission(int index) {
        return (this.permissions & index) != 0 || MinecraftHeadsResources.isDebug();
    }

    public static MchTier minTierRequired(int permission) {
        for (MchTier tier : ALL) {
            if (tier.hasPermission(permission)) {
                return tier;
            }
        }

        throw new IllegalArgumentException("Unknown permission index: " + Integer.numberOfTrailingZeros(permission));
    }

    public static MchTier parse(String id) {
        return switch (id) {
            case "none" -> NO_LICENSE;
            case "free" -> FREE;
            case "bronze" -> BRONZE;
            case "silver" -> SILVER;
            case "gold" -> GOLD;
            default -> {
                FzmmClient.LOGGER.warn("[MCHTier] Invalid license: '{}'", id);
                yield INVALID_LICENSE;
            }
        };
    }

    private static Builder builder() {
        return new Builder();
    }

    private static Builder builderOf(MchTier tier) {
        Builder result = builder();
        result.permissions = tier.permissions;
        return result;
    }

    static {
        // https://minecraft-heads.com/wiki/minecraft-heads/api-v2-license-types
        String translation = "fzmm.gui.headGallery.tier.name.";
        INVALID_LICENSE = builder()
                .message(
                        Component.translatable(translation + "invalid")
                                .setStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(true))
                )
                .build();
        NO_LICENSE = builder()
                .message(
                        Component.translatable(translation + "no_license")
                                .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true))
                )
                .set(HEADS_GENERAL_REQUEST)
                .set(HEADS_BASIC_DATA)
                .set(HEADS_BEST_HEADS)
                .set(CATEGORY_GENERAL_REQUEST)
                .build();
        FREE = builderOf(NO_LICENSE)
                .message(
                        Component.translatable(translation + "free")
                                .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
                )
                .set(HEADS_ADD_DATA_FREE)
                .set(COLLECTION_GENERAL_REQUEST)
                .build();
        BRONZE = builderOf(FREE)
                .message(
                        Component.translatable(translation + "bronze")
                                .setStyle(Style.EMPTY.withColor(0xFF_AE6800).withBold(true))
                )
                .build();
        SILVER = builderOf(BRONZE)
                .message(
                        Component.translatable(translation + "silver")
                                .setStyle(Style.EMPTY.withColor(0xFF_C0C0C0).withBold(true))
                )
                .set(HEADS_ALL_HEADS)
                .set(HEADS_ADD_DATA_TAGS)
                .set(HEADS_EARLY_ACCESS)
                .set(TAG_GENERAL_REQUEST)
                .set(COLLECTION_PLAYERS_GENERAL_REQUEST)
                .build();
        GOLD = builderOf(SILVER)
                .message(
                        Component.translatable(translation + "gold")
                                .setStyle(Style.EMPTY.withColor(0xFF_FFC107).withBold(true))
                )
                .set(HEADS_TRANSLATIONS)
                .set(CATEGORY_TRANSLATIONS)
                .set(TAG_TRANSLATIONS)
                .build();

        ALL = new MchTier[] {
                NO_LICENSE,
                FREE,
                BRONZE,
                SILVER,
                GOLD
        };
    }


    private final static class Builder {
        private Component message = Component.empty();
        private int permissions = 0;

        private Builder() {

        }

        private Builder message(Component text) {
            this.message = text;
            return this;
        }

        private Builder set(int index) {
            this.permissions |= index;
            return this;
        }

        private MchTier build() {
            return new MchTier(this.message, this.permissions);
        }
    }
}
