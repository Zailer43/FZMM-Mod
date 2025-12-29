package fzmm.zailer.me.client.logic.mineskin.api;

import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.mineskin.model.MSJob;
import fzmm.zailer.me.client.logic.mineskin.model.MSQueue;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MSApiQueues extends AbstractMineskinApi {

    public CompletableFuture<ApiResponse<MSQueue>> fetch(MSJob job) {
        return this.fetchData(MSQueue::parse, this.buildApiUrl("queue/" + job.id()));
    }

    @Override
    protected HttpRequestBase getRequest(String url) {
        return new HttpGet(url);
    }

    @Nullable
    @Override
    public Long parseDelayFromResponse(ApiResponse<?> response) {
        Long delay = super.parseDelayFromResponse(response);
        return delay == null || delay == 0 ? 1500L : delay;
    }
}