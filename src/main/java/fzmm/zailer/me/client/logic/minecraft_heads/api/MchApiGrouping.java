package fzmm.zailer.me.client.logic.minecraft_heads.api;

import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchCategory;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTag;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTier;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MchApiGrouping extends AbstractMchApi {

    public CompletableFuture<ApiResponse<List<MchCategory>>> fetchCategories() {
        // always accept request to the categories to be able to update the detected license
        String url = this.buildApiUrl("heads/categories");
        return this.fetchData(json -> this.parse(json, MchCategory::parse), url, this.requestOf(url).build());
    }

    public CompletableFuture<ApiResponse<List<MchTag>>> fetchTags(MchTier license) {
        if (!license.hasPermission(MchTier.TAG_GENERAL_REQUEST)) return ApiResponse.futureOf(List.of());

        String url = this.buildApiUrl("heads/tags");
        return this.fetchData(json -> this.parse(json, MchTag::parse), url, this.requestOf(url).build());
    }
}
