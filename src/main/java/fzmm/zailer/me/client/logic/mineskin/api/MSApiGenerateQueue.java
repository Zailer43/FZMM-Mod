package fzmm.zailer.me.client.logic.mineskin.api;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.mineskin.model.MSQueue;
import fzmm.zailer.me.client.logic.mineskin.model.MSVariant;

import java.net.http.HttpRequest;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class MSApiGenerateQueue extends AbstractMineskinApi {

    public CompletableFuture<ApiResponse<MSQueue>> submit(byte[] skin) {
        if (skin.length == 0) return CompletableFuture.completedFuture(null);

        String url = this.buildApiUrl("queue");
        HttpRequest.Builder request = this.requestOf(url);
        JsonObject body = this.getBody(skin);

        // #requestOf already has the json-content header
        request.POST(HttpRequest.BodyPublishers.ofString(body.toString()));

        return this.fetchData(MSQueue::parse, url, request.build());
    }

    // https://docs.mineskin.org/docs/mineskin-api/queue-skin-generation
    protected JsonObject getBody(byte[] skin) {
        JsonObject result = new JsonObject();

        result.addProperty("variant", MSVariant.UNKNOWN.value());
        //result.addProperty("name", );
        result.addProperty("visibility", FzmmClient.CONFIG.mineskin.visibility().value());
        //result.addProperty("cape", );
        result.addProperty("url", this.getSkinUrl(skin));

        return result;
    }

    protected String getSkinUrl(byte[] skin) {
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(skin);
    }
}
