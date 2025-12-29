package fzmm.zailer.me.client.logic.mineskin;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.snack_bar.SnackBarBuilder;
import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.mineskin.api.MSApiGenerateQueue;
import fzmm.zailer.me.client.logic.mineskin.api.MSApiQueues;
import fzmm.zailer.me.client.logic.mineskin.model.MSJob;
import fzmm.zailer.me.client.logic.mineskin.model.MSQueue;
import fzmm.zailer.me.utils.ImageUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

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
        return this.uploadSequential(skin);
    }

    private CompletableFuture<ApiResponse<MSQueue>> uploadSequential(BufferedImage skin) {
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

    public CompletableFuture<Void> uploadSequentially(List<BufferedImage> skins, BiConsumer<BufferedImage, ApiResponse<MSQueue>> callback,
                                                      AtomicBoolean isCancelled) {

        return CompletableFuture.runAsync(() -> {
            for (var skin : skins) {
                this.uploadSequential(skin).thenApply(response -> {
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

    public long getWaitMillis() {
        return this.generateApi.getWaitMillis();
    }

    public Optional<SnackBarBuilder> statusCodeAlert(@Nullable ApiResponse<?> response) {
        return this.generateApi.statusCodeAlert(response);
    }
}
