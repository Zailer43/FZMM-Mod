package fzmm.zailer.me.client.logic.headGallery;

import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record MinecraftHeadsData(String name, UUID uuid, String value, Set<String> tags) {

    public static MinecraftHeadsData parse(JsonObject jsonObject) {
        String name = jsonObject.get("name").getAsString();
        UUID uuid = UUID.fromString(jsonObject.get("uuid").getAsString());
        String value = jsonObject.get("value").getAsString();
        Set<String> tags = new HashSet<>(Arrays.asList(jsonObject.get("tags").getAsString().split(",")));
        return new MinecraftHeadsData(name, uuid, value, tags);
    }
}
