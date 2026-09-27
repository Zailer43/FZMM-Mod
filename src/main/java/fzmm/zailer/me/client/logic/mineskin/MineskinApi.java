package fzmm.zailer.me.client.logic.mineskin;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.SnackBarBuilder;
import fzmm.zailer.me.client.gui.components.snack_bar.UpdatableSnackBarComponent;
import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.mineskin.api.MSApiGenerateQueue;
import fzmm.zailer.me.client.logic.mineskin.api.MSApiQueues;
import fzmm.zailer.me.client.logic.mineskin.model.MSJob;
import fzmm.zailer.me.client.logic.mineskin.model.MSQueue;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MineskinApi {
    private static final int FETCH_MAX_REQUEST = 7;
    private final MSApiGenerateQueue generateApi = new MSApiGenerateQueue();
    private final MSApiQueues queuesApi = new MSApiQueues();

    public CompletableFuture<ApiResponse<MSQueue>> submit(BufferedImage skin) {
        byte[] skinBytes = ImageUtils.toByteArray(skin);
        if (skinBytes == null) return CompletableFuture.completedFuture(null);

        return this.generateApi.submit(skinBytes);
    }

    public CompletableFuture<ApiResponse<MSQueue>> fetch(MSJob job) {
        return this.queuesApi.fetch(job);
    }

    public CompletableFuture<ApiResponse<MSQueue>> upload(BufferedImage skin) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            ISnackBarComponent snackBar = BaseSnackBarComponent.builder(SnackBarManager.MINESKIN_ID)
                    .title(net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.mineskin.loading"))
                    .backgroundColor(EStyles.ALERT_LOADING_COLOR)
                    .keepOnLimit()
                    .build();
            SnackBarManager.getInstance().add(snackBar);
        });

        return this.uploadSequential(skin, false).thenApply(response -> {
            client.execute(() -> SnackBarManager.getInstance().remove(SnackBarManager.MINESKIN_ID));
            return response;
        });
    }

    public void showComplete(@Nullable ApiResponse<MSQueue> response, boolean generated, Consumer<ButtonComponent> retryConsumer) {
        if (generated) {
            this.showSuccess();
        } else {
            Optional<SnackBarBuilder> builder = FzmmClient.MINESKIN_API.statusCodeAlert(response);
            if (builder.isEmpty()) return;
            // replace snack bar but with the retry button
            SnackBarManager.getInstance().add(
                    builder.get().button(snackBar ->
                            EComponents.button(Component.translatable("fzmm.snack_bar.mineskin.error.button.retry")).onPress(retryConsumer)
                    ).build()
            );
        }
    }

    public void showSuccess() {
        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder(SnackBarManager.MINESKIN_ID)
                .title(Component.translatable("fzmm.snack_bar.mineskin.success"))
                .backgroundColor(EStyles.ALERT_SUCCESS_COLOR)
                .lowTimer()
                .startTimer()
                .build()
        );
    }

    private CompletableFuture<ApiResponse<MSQueue>> uploadSequential(BufferedImage skin, boolean isCancelled) {
        if (isCancelled) return CompletableFuture.failedFuture(new CancellationException());
        CompletableFuture<ApiResponse<MSQueue>> result = this.submit(skin);

        // queue can take a while to process, so it can need to be fetched multiple times
        for (int i = 0; i != FETCH_MAX_REQUEST; i++) {
            AtomicBoolean retry = new AtomicBoolean(true);
            result = result.thenCompose(response -> {
                this.updateShouldRetry(response, retry);
                if (!retry.get()) return CompletableFuture.completedFuture(response);
                return this.fetch(response.data().orElseThrow().job());
            });
        }

        result.whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                FzmmClient.LOGGER.error("[MineskinApi] Error processing response", throwable);
            }
        });

        return result;
    }

    private void updateShouldRetry(ApiResponse<MSQueue> response, AtomicBoolean retry) {
        if (response == null || !response.isSuccess()) {
            retry.set(false);
            return;
        }

        if (response.data().isEmpty()) return;
        MSQueue queue = response.data().get();

        if (queue.job().status().isDone() || queue.skin().isPresent()) {
            retry.set(false);
        }
    }

    public CompletableFuture<Void> uploadSequentially(AtomicBoolean isCancelled, List<BufferedImage> skins, BiConsumer<BufferedImage, ApiResponse<MSQueue>> callback) {

        return CompletableFuture.runAsync(() -> {
            for (var skin : skins) {
                this.uploadSequential(skin, isCancelled.get()).thenApply(response -> {
                    if (isCancelled.get()) return CompletableFuture.failedFuture(new CancellationException());
                    callback.accept(skin, response);
                    return null;
                }).join();
            }
        }).whenComplete((unused, throwable) -> {
            if (isCancelled.get()) {
                FzmmClient.LOGGER.info("[MineskinApi] Request cancelled");
            }
        });
    }

    public UpdatableSnackBarComponent showProcessing(int total, Consumer<ButtonComponent> cancelExecute) {
        var result = (UpdatableSnackBarComponent) UpdatableSnackBarComponent.builder(SnackBarManager.MINESKIN_ID)
                .backgroundColor(EStyles.ALERT_LOADING_COLOR)
                .keepOnLimit()
                .title(Component.translatable("fzmm.snack_bar.mineskin.sequentially.title", 0, total))
                .details(Component.translatable("fzmm.snack_bar.mineskin.sequentially.details", 0))
                .sizing(Sizing.fixed(220), Sizing.content())
                .startTimer()
                .button(snackBar -> EComponents.button(Component.translatable("fzmm.gui.button.cancel")).onPress(button -> {
                    cancelExecute.accept(button);
                    SnackBarManager.getInstance().remove(SnackBarManager.MINESKIN_ID);
                }))
                .build();

        SnackBarManager.getInstance().add(result);

        return result;
    }

    public void updateProcessing(UpdatableSnackBarComponent snackBar, int generated, int total) {
        float delay = this.getWaitMillis() / 1000f;
        snackBar.updateTitle(Component.translatable("fzmm.snack_bar.mineskin.sequentially.title", generated, total));
        snackBar.updateDetails(Component.translatable("fzmm.snack_bar.mineskin.sequentially.details", new DecimalFormat("#,#0.0").format(delay)));
        snackBar.updateTimerBar(generated / (float) total);

        if (!snackBar.hasParent()) {
            SnackBarManager.getInstance().add(snackBar);
        }
    }

    public long getWaitMillis() {
        return this.generateApi.getWaitMillis();
    }

    public Optional<SnackBarBuilder> statusCodeAlert(@Nullable ApiResponse<?> response) {
        return this.generateApi.statusCodeAlert(response);
    }
}
