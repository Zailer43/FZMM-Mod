package fzmm.zailer.me.client.logic.minecraft_heads.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.minecraft_heads.IMchMatcher;

import java.util.Arrays;
import java.util.Objects;

public class MchCollection implements IMchMatcher {
    private final String name;
    private final int[] heads;

    protected MchCollection(String name, int[] heads) {
        this.name = name;
        this.heads = heads;
    }

    // https://minecraft-heads.com/wiki/minecraft-heads/api-v2-documentation
    public static MchCollection parse(JsonObject json) throws UnsupportedOperationException, IllegalStateException {
        String name = json.get("n").getAsString();
        JsonArray headList = json.get("h").getAsJsonArray();
        int[] heads = new int[headList.size()];
        for (int i = 0; i != heads.length; i++) {
            heads[i] = headList.get(i).getAsInt();
        }

        return new MchCollection(name, heads);
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public boolean test(MchHead head) {
        if (head.id() == null) return false;

        for (int id : this.heads) {
            if (id == head.id()) return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MchCollection that = (MchCollection) o;
        return Objects.equals(this.name, that.name) && Objects.deepEquals(this.heads, that.heads);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, Arrays.hashCode(this.heads));
    }
}
