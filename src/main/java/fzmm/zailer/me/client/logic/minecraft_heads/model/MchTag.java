package fzmm.zailer.me.client.logic.minecraft_heads.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.minecraft_heads.IMchMatcher;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class MchTag implements IMchMatcher {
    public static final MchTag FAIL = new MchTag(-1, "UNMATCHED_TAG");
    private final int id;
    private final String name;
    private int useCount = 0;

    protected MchTag(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // https://minecraft-heads.com/wiki/minecraft-heads/api-v2-documentation
    public static MchTag parse(JsonObject json) throws UnsupportedOperationException, IllegalStateException {
        int id = json.get("id").getAsInt();
        String name = json.get("n").getAsString();

        return new MchTag(id, name);
    }

    public int id() {
        return this.id;
    }

    @Override
    public String name() {
        return this.name;
    }

    public Text text(boolean included) {
        Formatting color = included ? Formatting.GREEN : Formatting.RED;
        return Text.literal(this.formattedName())
                .setStyle(Style.EMPTY.withUnderline(true).withColor(color));
    }

    public String formattedName() {
        return this.name() + " (" + this.useCount + ")";
    }

    @Override
    public boolean test(MchHead head) {
        if (head.tags().isEmpty()) return false;

        for (var tag : head.tags().get()) {
            if (tag.id() == this.id) return true;
        }

        return false;
    }

    public void useCount(int value) {
        this.useCount = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MchTag mchTag = (MchTag) o;
        return this.id == mchTag.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
