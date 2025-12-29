package fzmm.zailer.me.client.logic.minecraft_heads.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

public class MchHead {
    protected final String name;
    // the value is a base64 of a json with a url,
    // the texture value of that url is a string of 64 characters in hexadecimal
    // and there are around 100k heads, using only its bytes saves some MB of ram
    protected final byte[] valueBytes;
    protected final MchCategory category;
    protected final UUID uuid;
    @Nullable
    protected final Integer id;
    @Nullable
    protected final MchTag[] tags;
    @Nullable
    protected final LocalDate publishedAt;

    protected MchHead(String name, byte[] valueBytes, MchCategory category, UUID uuid, @Nullable Integer id,
                      @Nullable LocalDate publishedAt, @Nullable MchTag[] tags) {
        this.name = name;
        this.valueBytes = valueBytes;
        this.category = category;
        this.uuid = uuid;
        this.id = id;
        this.tags = tags;
        this.publishedAt = publishedAt;
    }

    // https://minecraft-heads.com/wiki/minecraft-heads/api-v2-documentation
    public static MchHead parse(JsonObject json) throws UnsupportedOperationException, IllegalStateException {
        String name = json.get("n").getAsString();

        // always gets the value as url, since it needs to be cached,
        // as there are so many entries less data is better
        byte[] valueBytes = deserializeValue(json.get("u").getAsString());

        MchCategory category = FzmmClient.MCH_RESOURCES.categoryFrom(json.get("c").getAsInt());

        UUID uuid;
        if (json.has("i")) {// head uuid
            uuid = UUID.fromString(json.get("i").getAsString());
        } else {
            uuid = valueToUuid(valueBytes); // free license don't have uuid, make one from valueBytes to be consistent
        }

        // minecraft-heads ID, free license
        Integer id = json.has("id") ? json.get("id").getAsInt() : null;

        // minecraft-heads published date, free license
        LocalDate publishedAt = null;
        if (json.has("p")) {
            String date = json.get("p").getAsString();
            publishedAt = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        }

        // minecraft-heads tags, silver license
        MchTag[] tags = json.has("t") ? FzmmClient.MCH_RESOURCES.tagsFrom(json.get("t").getAsJsonArray()) : null;

        return new MchHead(name, valueBytes, category, uuid, id, publishedAt, tags);
    }

    /**
     * Builds a UUID from the last 16 bytes of an array of bytes
     * @return If the array is less than 16 bytes, returns a random UUID
     */
    private static UUID valueToUuid(byte[] arrayBytes) {
        if (arrayBytes.length < 16) return UUID.randomUUID();

        byte[] last16Bytes = new byte[16];
        System.arraycopy(arrayBytes, arrayBytes.length - 16, last16Bytes, 0, 16);

        long mostSigBits = 0;
        long leastSigBits = 0;

        for (int i = 0; i < 8; i++) {
            mostSigBits = (mostSigBits << 8) | (last16Bytes[i] & 0xFF);
        }
        for (int i = 8; i < 16; i++) {
            leastSigBits = (leastSigBits << 8) | (last16Bytes[i] & 0xFF);
        }

        return new UUID(mostSigBits, leastSigBits);
    }

    public static byte[] deserializeValue(String urlValue) {
        // it is necessary to handle even and odd numbers because apparent the size of the URL value may vary
        int length = (urlValue.length() + 1) / 2; // number of bytes
        byte[] result = new byte[length + 1]; // isOdd + bytes

        result[0] = (byte) (urlValue.length() & 0b1);
        if (result[0] == 1) {
            urlValue += "0";
        }

        byte[] parsed = HexFormat.of().parseHex(urlValue);
        System.arraycopy(parsed, 0, result, 1, parsed.length);

        return result;
    }

    private static String serializeValue(byte[] valueBytes) {
        // it is necessary to handle even and odd numbers because apparent the size of the URL value may vary
        boolean isOdd = valueBytes[0] == 1;
        String parsed = HexFormat.of().formatHex(valueBytes, 1, valueBytes.length);

        if (isOdd) {
            return parsed.substring(0, parsed.length() - 1);
        } else {
            return parsed;
        }
    }

    public String name() {
        return this.name;
    }

    public String convertRawValue() {
        return serializeValue(this.valueBytes);
    }

    public MchCategory category() {
        return this.category;
    }

    public UUID uuid() {
        return this.uuid;
    }

    @Contract(pure = true)
    public Integer id() {
        return this.id;
    }

    public Optional<MchTag[]> tags() {
        return Optional.ofNullable(this.tags);
    }

    @Contract(pure = true)
    public LocalDate publishedAt() {
        return this.publishedAt;
    }

    public ItemStack toStack() {
        return HeadBuilder.builder()
                .urlValue(this.convertRawValue())
                .id(this.uuid())
                .notAddToHistory()
                .get();
    }
}
