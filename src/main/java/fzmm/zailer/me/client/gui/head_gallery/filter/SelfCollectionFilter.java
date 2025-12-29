package fzmm.zailer.me.client.gui.head_gallery.filter;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.minecraft_heads.IMchMatcher;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTier;
import fzmm.zailer.me.utils.SnackBarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SelfCollectionFilter extends AbstractFilter {

    public SelfCollectionFilter(Consumer<IMchMatcher> applyOption) {
        super(applyOption);
    }

    @Override
    public CompletableFuture<ApiResponse<List<IMchMatcher>>> initOptions() {
        ISnackBarComponent snackBar = BaseSnackBarComponent.builder(SnackBarManager.HEAD_GALLERY_ID)
                .backgroundColor(EStyles.ALERT_LOADING_COLOR)
                .title(this.loadingAlertText())
                .build();
        SnackBarManager.getInstance().add(snackBar);

        return this.collections().whenComplete((ignored, throwable) ->
                Minecraft.getInstance().execute(snackBar::close)
        );
    }

    @Override
    protected Component filterText() {
        return Component.translatable("fzmm.gui.headGallery.button.collections.self");
    }

    @Override
    protected Component missingOptions() {
        return Component.translatable("fzmm.gui.headGallery.option.collection.self.missing");
    }

    @Override
    public int permissionRequired() {
        return MchTier.COLLECTION_GENERAL_REQUEST;
    }

    protected CompletableFuture<ApiResponse<List<IMchMatcher>>> collections() {
        return FzmmClient.MCH_RESOURCES.fetchSelfCollection();
    }

    protected Component loadingAlertText() {
        return Component.translatable("fzmm.gui.headGallery.snack_bar.loading.collection.self");
    }
}
