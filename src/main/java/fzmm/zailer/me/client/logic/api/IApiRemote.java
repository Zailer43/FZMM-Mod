package fzmm.zailer.me.client.logic.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.util.Util;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface IApiRemote extends IApiBase {
    String HTTP_USER_AGENT = "FZMM/2.0";

    String baseApiUrl();

    String buildApiUrl(String route, String... parameters);

    default String buildParameters(String... parameters) {
        StringBuilder paramBuilder = new StringBuilder();
        for (var parameter : parameters) {
            if (parameter != null) {
                paramBuilder.append("&").append(parameter);
            }
        }
        return paramBuilder.toString();
    }

    default CompletableFuture<Void> prepare() {
        return CompletableFuture.completedFuture(null);
    }

    default <T> CompletableFuture<ApiResponse<T>> fetchUrl(String url, HttpRequest request) {
        return this.prepare().thenApplyAsync(unused -> {
            try (var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()) {
                HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
                ApiResponse<T> response = new ApiResponse<>(httpResponse.statusCode());

                String body = httpResponse.body();
                if (body != null && !body.isEmpty()) {
                    response.json(this.parseResponse(body));
                    this.logWarnings(url.substring(this.baseApiUrl().length()), response);
                    this.parseSuccess(response);

//                    if (Owo.DEBUG) {
//                        FzmmClient.LOGGER.info("[IApiRemote] [DEBUG] Raw response: {}", response.json());
//                    }

                    if (response.json().isEmpty() || response.statusType() == 5) {
                        response.success(false);
                    } else {
                        this.processResponse(response, response.statusCode() == 4);
                    }

                } else {
                    response.success(false);
                }

                return response;
            } catch (Exception e) {
                FzmmClient.LOGGER.error("[IApiRemote] Error processing response", e);
                return ApiResponse.invalid();
            }
        }, Util.nonCriticalIoPool());
    }

    void parseSuccess(ApiResponse<?> response);

    default <T> CompletableFuture<ApiResponse<T>> fetchData(Function<JsonObject, T> parser, String url, HttpRequest request) {
        return this.<T>fetchUrl(url, request).thenApply(response -> this.parseModel(parser, response));
    }

    HttpRequest.Builder requestOf(String url);

    default JsonObject parseResponse(String body) {
        return JsonParser.parseString(body).getAsJsonObject();
    }
}
