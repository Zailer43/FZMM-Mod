package fzmm.zailer.me.client.logic.minecraft_heads.api;

import com.mojang.authlib.GameProfile;
import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.minecraft_heads.IMchMatcher;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchCollection;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTier;
import fzmm.zailer.me.utils.skin.CacheSkinGetter;
import fzmm.zailer.me.utils.skin.VanillaSkinGetter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MchApiCollection extends AbstractMchApi {

    public CompletableFuture<ApiResponse<List<IMchMatcher>>> fetchOneselfCollections(MchTier license) {
        if (!license.hasPermission(MchTier.COLLECTION_GENERAL_REQUEST)) return ApiResponse.futureInvalid();

        return this.fetchData(json -> this.parse(json, MchCollection::parse), this.buildApiUrl("heads/collections"));
    }

    public CompletableFuture<ApiResponse<List<IMchMatcher>>> fetchPlayerCollections(MchTier license, String playerName) {
        // first try to get from cache to avoid uuid request
        Optional<GameProfile> profileOptional = new CacheSkinGetter(new VanillaSkinGetter()).getProfile(playerName);

        return profileOptional.isEmpty() ? ApiResponse.futureInvalid() : this.fetchPlayerCollections(license, profileOptional.get().id());
    }

    public CompletableFuture<ApiResponse<List<IMchMatcher>>> fetchPlayerCollections(MchTier license, UUID playerUuid) {
        if (!license.hasPermission(MchTier.COLLECTION_PLAYERS_GENERAL_REQUEST)) return ApiResponse.futureInvalid();

        String uuid = "player_uuid=" + playerUuid.toString();
        return this.fetchData(json -> this.parse(json, MchCollection::parse), this.buildApiUrl("heads/collections/player", uuid));
    }

    @Override
    public long defaultDelayMillis() {
        return 6000L;
    }
}
