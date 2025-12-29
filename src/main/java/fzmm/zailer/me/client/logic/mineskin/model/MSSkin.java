package fzmm.zailer.me.client.logic.mineskin.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.HeadUtils;

import java.util.Optional;

public class MSSkin {
    protected final String uuid;
    protected final String value;

    protected MSSkin(String uuid, String value) {
        this.uuid = uuid;
        this.value = value;
    }

    public static MSSkin parse(JsonObject jsonObject) {
        String uuid = jsonObject.get("uuid").getAsString();
        String value = jsonObject.get("texture").getAsJsonObject()
                .get("data").getAsJsonObject()
                .get("value").getAsString();

        MSSkin skin = new MSSkin(uuid, value);
        if (!FzmmClient.CONFIG.general.minimizeHeadTexturesTag()) return skin;

        Optional<String> unwrappedUrl = HeadUtils.unwrapUrl(value);
        return unwrappedUrl.map(s -> new MSSkin(uuid, HeadUtils.wrapUrl(s))).orElse(skin);
    }

    public HeadBuilder builder() {
        return HeadBuilder.builder()
                .skinValue(this.value);
    }

    public String toSkinValue() {
        return this.value;
    }
}
