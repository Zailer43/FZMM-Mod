package fzmm.zailer.me.client.logic.minecraft_heads.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.minecraft_heads.IMchMatcher;

import java.util.Objects;

public class MchCategory implements IMchMatcher {
    public static final MchCategory FAIL = new MchCategory(-1, "UNMATCHED_CATEGORY");
    public static final MchCategory ALL = new MchCategory(-2, "All");
    private final int id;
    private final String name;

    protected MchCategory(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // https://minecraft-heads.com/wiki/minecraft-heads/api-v2-documentation
    public static MchCategory parse(JsonObject json) throws UnsupportedOperationException, IllegalStateException {
        int id = json.get("id").getAsInt();
        String name = json.get("n").getAsString();

        return new MchCategory(id, name);
    }

    public int id() {
        return this.id;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public boolean test(MchHead head) {
        return head.category().id() == this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MchCategory that = (MchCategory) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
