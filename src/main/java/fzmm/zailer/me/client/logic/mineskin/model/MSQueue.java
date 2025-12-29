package fzmm.zailer.me.client.logic.mineskin.model;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// https://docs.mineskin.org/docs/mineskin-api/queue-skin-generation/
public class MSQueue {
    protected final MSJob job;
    @Nullable
    protected final MSSkin skin;

    protected MSQueue(MSJob job, @Nullable MSSkin skin) {
        this.job = job;
        this.skin = skin;
    }

    public static MSQueue parse(JsonObject jsonObject) {
        MSJob job = MSJob.parse(jsonObject.getAsJsonObject("job"));

        MSSkin skin = null;
        if (jsonObject.has("skin")) {
            skin = MSSkin.parse(jsonObject.getAsJsonObject("skin"));
        }

        return new MSQueue(job, skin);
    }

    public MSJob job() {
        return this.job;
    }

    public String id() {
        return this.job.id();
    }

    public Optional<MSSkin> skin() {
        return Optional.ofNullable(this.skin);
    }
}
