package fzmm.zailer.me.client.logic.minecraft_heads;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.SnackBarBuilder;
import fzmm.zailer.me.client.gui.components.snack_bar.UpdatableSnackBarComponent;
import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.minecraft_heads.api.MchApiCollection;
import fzmm.zailer.me.client.logic.minecraft_heads.api.MchApiGrouping;
import fzmm.zailer.me.client.logic.minecraft_heads.api.MchApiHead;
import fzmm.zailer.me.client.logic.minecraft_heads.model.*;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.Observable;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MinecraftHeadsResources {
    // There are hash maps instead of lists because it is necessary to preserve the ID given by minecraft-heads
    private final Int2ObjectLinkedOpenHashMap<MchCategory> categories = new Int2ObjectLinkedOpenHashMap<>();
    private final Int2ObjectLinkedOpenHashMap<MchTag> tags = new Int2ObjectLinkedOpenHashMap<>();
    private final Object2ObjectLinkedOpenHashMap<String, List<IMchMatcher>> collections = new Object2ObjectLinkedOpenHashMap<>();
    private final MchApiCollection collectionApi = new MchApiCollection();
    private final MchApiGrouping groupingApi = new MchApiGrouping();
    private final MchApiHead headApi = new MchApiHead();
    private Observable<Int2ObjectLinkedOpenHashMap<MchHead>> heads = Observable.of(new Int2ObjectLinkedOpenHashMap<>());
    private List<IMchMatcher> selfCollection = null;
    private MchTier license = MchTier.INVALID_LICENSE;
    private boolean inProgress = false;

    public MinecraftHeadsResources() {
    }

    public void license(JsonObject jsonResponse) {
        if (!jsonResponse.has("meta") || !jsonResponse.get("meta").isJsonObject()) return;

        JsonObject meta = jsonResponse.get("meta").getAsJsonObject();
        MchTier license = MchTier.parse(meta.get("license").getAsString());
        if (this.license != license) {
            this.license = license;
            String apiVersion = meta.has("api_version") ? meta.get("api_version").getAsString() : "unknown";
            FzmmClient.LOGGER.info("[MinecraftHeadsResources] API Version '{}' and detected license '{}'",
                    apiVersion, license.message().getString()
            );
        }
    }

    public CompletableFuture<Void> fetchEssential() {
        if (!this.categories.isEmpty() || !this.heads.get().isEmpty() || this.inProgress) {
            return CompletableFuture.completedFuture(null);
        }
        this.inProgress = true;

        UpdatableSnackBarComponent loadingSnackBar = (UpdatableSnackBarComponent) UpdatableSnackBarComponent.builder(SnackBarManager.HEAD_GALLERY_ID)
                .backgroundColor(EStyles.ALERT_LOADING_COLOR)
                .title(Text.translatable("fzmm.gui.headGallery.snack_bar.loading.title"))
                .details(Text.translatable("fzmm.gui.headGallery.snack_bar.loading.essential.categories"))
                .keepOnLimit()
                .expandDetails()
                .sizing(Sizing.fixed(200), Sizing.content())
                .build();

        SnackBarManager.getInstance().add(loadingSnackBar);

        FzmmClient.LOGGER.info("[MinecraftHeadsResources] Fetching categories, tags (if license allow it) and heads...");
        return this.categoriesStage(loadingSnackBar)
                .thenCompose(unused -> this.tagsStage(loadingSnackBar))
                .thenCompose(unused -> this.headsStage(loadingSnackBar))
                .whenComplete(this::completeFetch);
    }

    private CompletableFuture<ApiResponse<List<MchTag>>> categoriesStage(UpdatableSnackBarComponent loadingSnackBar) {
        return this.groupingApi.fetchCategories().thenApply(response -> {
            if (response.data().isEmpty() || response.data().get().isEmpty()) {
                throw new IllegalStateException("Minecraft-heads categories not found");
            }
            List<MchCategory> categories = response.data().get();
            FzmmClient.LOGGER.info("[MinecraftHeadsResources] Fetched {} categories", categories.size());

            for (var category : categories) {
                this.categories.put(category.id(), category);
            }

            MinecraftClient.getInstance().execute(() ->
                    loadingSnackBar.updateDetails(Text.translatable("fzmm.gui.headGallery.snack_bar.loading.essential.tags", categories.size()))
            );

            return null;
        });
    }

    private CompletableFuture<Void> tagsStage(UpdatableSnackBarComponent loadingSnackBar) {
        // license is detected and set with first api fetch (categories)
        return this.groupingApi.fetchTags(this.license).thenApply(response -> {
            if (response.data().isEmpty()) return null;
            List<MchTag> tags = response.data().get();
            FzmmClient.LOGGER.info("[MinecraftHeadsResources] Fetched {} tags", tags.size());

            for (var tag : tags) {
                this.tags.put(tag.id(), tag);
            }

            MinecraftClient.getInstance().execute(() ->
                    loadingSnackBar.updateDetails(Text.translatable("fzmm.gui.headGallery.snack_bar.loading.essential.heads",
                            this.categories.size(), this.tags.size(), 0, 0))
            );

            return null;
        });
    }

    private CompletableFuture<Void> headsStage(UpdatableSnackBarComponent loadingSnackBar) {
        Consumer<MchPagination> snackBarUpdate = (pagination) -> MinecraftClient.getInstance().execute(() -> {
            loadingSnackBar.startTimer();
            loadingSnackBar.updateTimerBar(pagination.percentage());
            loadingSnackBar.updateDetails(Text.translatable(
                    "fzmm.gui.headGallery.snack_bar.loading.essential.heads",
                    this.categories().size(), this.tags.size(), pagination.currentPage(), pagination.lastPage())
            );
        });

        return this.headApi.fetchAllHeads(this.license, snackBarUpdate, this::addHeads, !this.tags.isEmpty());
    }

    private void completeFetch(Void ignored, Throwable throwable) {
        boolean isError = throwable != null;
        MinecraftClient.getInstance().execute(() -> {
            SnackBarBuilder completeSnackBar = BaseSnackBarComponent.builder(SnackBarManager.HEAD_GALLERY_ID)
                    .backgroundColor(isError ? EStyles.ALERT_ERROR_COLOR : EStyles.ALERT_SUCCESS_COLOR)
                    .title(Text.translatable("fzmm.gui.headGallery.snack_bar." + (isError ? "error" : "successful") + ".title"))
                    .lowTimer()
                    .startTimer();

            if (isError) {
                FzmmClient.LOGGER.error("[HeadGalleryResources] API Exception", throwable);
                completeSnackBar.details(Text.translatable("fzmm.gui.headGallery.snack_bar.error.message"))
                        .highTimer()
                        .closeButton()
                        .expandDetails();
            }

            SnackBarManager.getInstance().add(completeSnackBar.build());
        });

        this.inProgress = false;
    }

    public CompletableFuture<ApiResponse<List<IMchMatcher>>> fetchSelfCollection() {
        if (!this.license.hasPermission(MchTier.COLLECTION_GENERAL_REQUEST)) return ApiResponse.futureInvalid();

        // self collection username would be different to minecraft name, that's why it's a different variable
        if (this.selfCollection != null) return ApiResponse.futureOf(this.selfCollection);

        FzmmClient.LOGGER.info("[MinecraftHeadsResources] Fetching self collections...");
        return this.collectionApi.fetchOneselfCollections(this.license)
                .whenComplete((response, throwable) -> {
                    if (response == null || throwable != null) {
                        FzmmClient.LOGGER.warn("[MinecraftHeadsResources] Failed to fetch self collections");
                    } else {
                        FzmmClient.LOGGER.info("[MinecraftHeadsResources] Fetched self collections");
                        response.data().ifPresent(collections -> this.selfCollection = collections);
                    }
                });
    }

    public CompletableFuture<ApiResponse<List<IMchMatcher>>> fetchCollectionFrom(String username) {
        if (!this.license.hasPermission(MchTier.COLLECTION_PLAYERS_GENERAL_REQUEST)) return ApiResponse.futureInvalid();

        String userLowercase = username.toLowerCase(Locale.ROOT);
        if (this.collections.containsKey(userLowercase)) {
            FzmmClient.LOGGER.info("[MinecraftHeadsResources] Using cached '{}' collections...", username);
            return ApiResponse.futureOf(this.collections.get(userLowercase));
        }

        if (this.collectionApi.getWaitMillis() != 0) {
            FzmmClient.LOGGER.warn("[MinecraftHeadsResources] Too many requests, please wait {}ms before next request", this.collectionApi.getWaitMillis());
            SnackBarManager.getInstance().add(BaseSnackBarComponent.builder(SnackBarManager.HEAD_GALLERY_ID)
                    .title(Text.translatable("fzmm.gui.headGallery.snack_bar.error.delay"))
                    .backgroundColor(EStyles.ALERT_WARNING_COLOR)
                    .lowTimer()
                    .startTimer()
                    .build()
            );
            return ApiResponse.futureInvalid();
        }

        FzmmClient.LOGGER.info("[MinecraftHeadsResources] Fetching '{}' collections...", username);
        return this.collectionApi.fetchPlayerCollections(this.license, username)
                .whenComplete((response, throwable) -> {
                    if (response == null || throwable != null) {
                        FzmmClient.LOGGER.warn("[MinecraftHeadsResources] Failed to fetch '{}' collections", username);
                    } else {
                        FzmmClient.LOGGER.info("[MinecraftHeadsResources] Fetched collections of '{}'", username);
                        response.data().ifPresent(collections -> this.collections.put(userLowercase, collections));
                    }
                });
    }

    public Collection<MchCategory> categories() {
        return this.categories.values();
    }

    public Collection<MchTag> tags() {
        return this.tags.values();
    }

    public Collection<MchHead> heads() {
        return this.heads.get().values();
    }

    private void addHeads(List<MchHead> heads) {
        Int2ObjectLinkedOpenHashMap<MchHead> headsMap = new Int2ObjectLinkedOpenHashMap<>(this.heads.get().size() + heads.size());
        headsMap.putAll(this.heads.get());

        for (var head : heads) {
            headsMap.put(head.id() == null ? headsMap.size() : head.id(), head);
        }

        for (var tag : this.tags.values()) {
            tag.useCount(tag.filter(headsMap.values()).size());
        }

        this.heads.set(headsMap);
    }

    public void onHeadsUpdate(Consumer<Int2ObjectLinkedOpenHashMap<MchHead>> observer) {
        this.heads.observe(observer);
    }

    public void clearObserver() {
        // this class is a singleton and observables need to be cleared
        this.heads = Observable.of(this.heads.get());
    }

    public MchTier licenseDetected() {
        return this.license;
    }

    public MchCategory categoryFrom(int id) {
        return this.categories.getOrDefault(id, MchCategory.FAIL);
    }

    public Text categoryText(IMchMatcher category) {
        boolean useTranslation = this.license.hasPermission(MchTier.CATEGORY_TRANSLATIONS) &&
                FzmmClient.CONFIG.minecraftHeads.useMchTranslations();

        // "Food & Drinks" -> "food-drinks"
        String id = category.name().toLowerCase().replace(" & ", "-");

        if (useTranslation || !I18n.hasTranslation(id)) {
            return Text.literal(category.name());
        } else {
            return Text.translatable("fzmm.gui.headGallery.button.filterOption." + id);
        }
    }

    public boolean hasTags() {
        return !this.tags.isEmpty();
    }

    public MchTag[] tagsFrom(JsonArray array) {
        if (this.tags.isEmpty()) return new MchTag[0];

        MchTag[] result = new MchTag[array.size()];

        for (int i = 0; i != result.length; i++) {
            result[i] = this.tags.getOrDefault(array.get(i).getAsInt(), MchTag.FAIL);
        }

        return result;
    }

    public List<MchHead> sinceId(int id) {
        List<MchHead> result = new ObjectArrayList<>();

        for (var head : this.heads.get().sequencedValues()) {
            if (head.id() == null) continue;

            if (head.id() > id) {
                result.add(head);
            }
        }

        return result;
    }

    /**
     * In debug mode it will to use demo mode from the api.
     * When this is enabled, missing permissions warnings will be ignored because demo mode should have all permissions.
     * Although in practice it missing Collection Players General Request permission and maybe other.
     *
     * @return true if is in IDE and player has not a player head in offhand
     */
    public static boolean isDebug() {
        assert MinecraftClient.getInstance().player != null;
        return Owo.DEBUG && MinecraftClient.getInstance().player.getOffHandStack().getItem() != Items.PLAYER_HEAD;
    }
}
