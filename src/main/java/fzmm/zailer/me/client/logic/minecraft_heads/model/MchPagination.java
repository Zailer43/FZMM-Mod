package fzmm.zailer.me.client.logic.minecraft_heads.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;

public class MchPagination {
    private final int total;
    private final int perPage;
    private final int currentPage;

    public MchPagination(int total, int perPage, int currentPage) {
        this.total = total;
        this.perPage = perPage;
        this.currentPage = currentPage;
    }

    // https://minecraft-heads.com/wiki/minecraft-heads/api-v2-documentation
    public static MchPagination parse(JsonObject json) throws UnsupportedOperationException, IllegalStateException {
        int total = json.get("total").getAsInt();
        int perPage = json.get("per_page").getAsInt();
        int currentPage = json.get("current_page").getAsInt();
        int lastPage = json.get("last_page").getAsInt();

        if (perPage <= 0) {
            throw new IllegalStateException("minecraft-heads returned an invalid number per page (per_page <= 0)");
        }
        if (total > (perPage * lastPage)) {
            FzmmClient.LOGGER.warn("[MchPagination] Invalid pagination (total={} per_page={} last_page={})", total, perPage, lastPage);
        }

        return new MchPagination(total, perPage, currentPage);
    }

    public int total() {
        return this.total;
    }

    public int currentPage() {
        return this.currentPage;
    }

    public int lastPage() {
        return (int) Math.ceil(this.total / (float) this.perPage);
    }

    public float percentage() {
        return (float) this.currentPage / (float) this.lastPage();
    }
}
