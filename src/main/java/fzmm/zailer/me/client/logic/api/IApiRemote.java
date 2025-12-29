package fzmm.zailer.me.client.logic.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.client.FzmmClient;
import io.wispforest.owo.Owo;
import net.minecraft.util.Util;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    default <T> CompletableFuture<ApiResponse<T>> fetchUrl(String url) {
        return this.fetchUrl(url, this.requestOf(url));
    }
    default <T> CompletableFuture<ApiResponse<T>> fetchUrl(String url, HttpRequestBase request) {
        return this.prepare().thenApplyAsync(unused -> {
            try (var httpClient = this.getHttpClient()) {
                HttpResponse httpResponse = httpClient.execute(request);
                ApiResponse<T> response = new ApiResponse<>(httpResponse.getStatusLine().getStatusCode());

                HttpEntity resEntity = httpResponse.getEntity();
                if (resEntity != null) {
                    response.json(this.parseResponse(resEntity));
                    this.logWarnings(url.substring(this.baseApiUrl().length()), response);
                    this.parseSuccess(response);

                    if (Owo.DEBUG) {
                        FzmmClient.LOGGER.info("[IApiRemote] [DEBUG] Raw response: {}", response.json());
                    }

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
        }, Util.getDownloadWorkerExecutor());
    }

    void parseSuccess(ApiResponse<?> response);

    default <T> CompletableFuture<ApiResponse<T>> fetchData(Function<JsonObject, T> parser, String url) {
        return this.<T>fetchUrl(url).thenApply(response -> this.parseModel(parser, response));
    }

    default <T> CompletableFuture<ApiResponse<T>> fetchData(Function<JsonObject, T> parser, String url, HttpRequestBase request) {
        return this.<T>fetchUrl(url, request).thenApply(response -> this.parseModel(parser, response));
    }

    HttpRequestBase requestOf(String url);

    default CloseableHttpClient getHttpClient() {
        return getHttpClient(HTTP_USER_AGENT);
    }

    static CloseableHttpClient getHttpClient(String userAgent) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(15000)
                .setSocketTimeout(15000)
                .build();

        return HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .disableAutomaticRetries()
                .setDefaultRequestConfig(requestConfig)
                .setUserAgent(userAgent)
                .build();
    }

    default JsonObject parseResponse(HttpEntity resEntity) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(resEntity.getContent()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            return JsonParser.parseString(response.toString()).getAsJsonObject();
        }
    }
}
