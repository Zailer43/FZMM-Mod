package fzmm.zailer.me.client.logic.api;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ApiResponse<T> {
    private final int statusCode;
    @Nullable
    private T data = null;
    @Nullable
    private JsonObject json = null;
    private boolean isSuccess = false;

    public ApiResponse(int statusCode) {
        this.statusCode = statusCode;

        if (this.statusType() != 2) {
            FzmmClient.LOGGER.warn("[ApiResponse] API response status code: {}", statusCode);
        }
    }

    public static <T> ApiResponse<T> of(T data) {
        ApiResponse<T> response = new ApiResponse<>(200);
        response.success(true);
        response.data(data);
        return response;
    }

    public static <T> CompletableFuture<ApiResponse<T>> futureOf(T data) {
        return CompletableFuture.completedFuture(of(data));
    }

    public static <T> ApiResponse<T> invalid() {
        return new ApiResponse<>(-1);
    }

    public static <T> CompletableFuture<ApiResponse<T>> futureInvalid() {
        return CompletableFuture.completedFuture(invalid());
    }

    public int statusCode() {
        return this.statusCode;
    }

    public int statusType() {
        return this.statusCode / 100;
    }

    public Optional<T> data() {
        return Optional.ofNullable(this.data);
    }

    public void data(T data) {
        this.data = data;
    }

    public Optional<JsonObject> json() {
        return Optional.ofNullable(this.json);
    }

    public void json(JsonObject json) {
        this.json = json;
    }

    public boolean isSuccess() {
        return this.isSuccess;
    }

    public void success(boolean success) {
        this.isSuccess = success;
    }

}
