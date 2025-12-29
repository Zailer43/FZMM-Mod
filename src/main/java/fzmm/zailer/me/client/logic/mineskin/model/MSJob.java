package fzmm.zailer.me.client.logic.mineskin.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.builders.HeadBuilder;
import org.jetbrains.annotations.Nullable;

// https://docs.mineskin.org/docs/mineskin-api/queue-skin-generation/
public class MSJob {
    protected final String id;
    protected MSJobStatus status;
    @Nullable
    protected String result;

    protected MSJob(String id, MSJobStatus status, @Nullable String result) {
        this.id = id;
        this.status = status;
        this.result = result;
    }

    public static MSJob parse(JsonObject jsonObject) {
        String id = jsonObject.get("id").getAsString(); // uuid is a 24 hex string, is an invalid UUID
        MSJobStatus status = MSJobStatus.parse(jsonObject.get("status").getAsString());
        String result = jsonObject.has("result") ? jsonObject.get("result").getAsString() : null;

        return new MSJob(id, status, result);
    }

    public String id() {
        return this.id;
    }

    public MSJobStatus status() {
        return this.status;
    }

    public boolean update(MSJob job) {
        boolean result = this.status != job.status && job.result != null;

        this.status = job.status;
        this.result = job.result;

        return result;
    }

    public HeadBuilder builder() {
        return HeadBuilder.builder()
                .urlValue(this.result);
    }
}
