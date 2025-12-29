package fzmm.zailer.me.client.logic.minecraft_heads.api;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchHead;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchPagination;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTier;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class MchApiHead extends AbstractMchApi {

    private String headsUrl(MchTier license, int page, boolean hasTags) {
        String pageParam = "page=" + page;
        String uuid = license.hasPermission(MchTier.HEADS_ADD_DATA_FREE) ? "uuid=true" : null;
        //String value = null; // it is less data using the url, and it is the same content
        String id = license.hasPermission(MchTier.HEADS_ADD_DATA_FREE) ? "id=true" : null;
        String publishedAt = license.hasPermission(MchTier.HEADS_ADD_DATA_FREE) ? "published_at=true" : null;
        String tags = (license.hasPermission(MchTier.HEADS_ADD_DATA_TAGS) && hasTags) ? "tags=true" : null;

        return this.buildApiUrl("heads/custom-heads", pageParam, uuid, id, publishedAt, tags);
    }

    private CompletableFuture<ApiResponse<List<MchHead>>> fetchHeads(MchTier license, int page, boolean hasTags,
                                                                     Function<JsonObject, List<MchHead>> parser) {
        if (!license.hasPermission(MchTier.HEADS_GENERAL_REQUEST) || !license.hasPermission(MchTier.HEADS_BASIC_DATA)) {
            return ApiResponse.futureInvalid();
        }

        return this.fetchData(parser, this.headsUrl(license, page, hasTags));
    }

    public CompletableFuture<Void> fetchAllHeads(MchTier license, Consumer<MchPagination> snackBarUpdate,
                                                 Consumer<List<MchHead>> headsUpdate, boolean hasTags) {
        if (!license.hasPermission(MchTier.HEADS_GENERAL_REQUEST) || !license.hasPermission(MchTier.HEADS_BASIC_DATA)) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            AtomicReference<MchPagination> lastPage = new AtomicReference<>(new MchPagination(1, 1, 1));
            int currentPage = 1;
            do {
                this.fetchHeads(license, currentPage, hasTags, json -> {
                    if (json.has("pagination") && json.get("pagination").isJsonObject()) {
                        lastPage.set(MchPagination.parse(json.getAsJsonObject("pagination").getAsJsonObject()));
                    }
                    return this.parse(json, MchHead::parse);
                }).whenComplete((response, throwable) -> {
                    if (throwable != null || response == null || !response.isSuccess()) {
                        FzmmClient.LOGGER.warn("[MchApiHead] Failed to fetch heads page {}", lastPage.get().currentPage());
                        return;
                    }
                    response.data().ifPresent(heads -> {
                        headsUpdate.accept(heads);
                        FzmmClient.LOGGER.info("[MchApiHead] Fetched heads page {}, with {} heads", lastPage.get().currentPage(),  heads.size());
                    });
                    snackBarUpdate.accept(lastPage.get());
                }).join();
            } while (currentPage++ < lastPage.get().lastPage());
        });
    }
}
