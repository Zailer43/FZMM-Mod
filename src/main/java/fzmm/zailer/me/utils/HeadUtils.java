package fzmm.zailer.me.utils;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class HeadUtils {

    public static CompletableFuture<Optional<SkinTextures>> getSkinTextures(ItemStack stack) {
        ProfileComponent profileComponent = stack.get(DataComponentTypes.PROFILE);
        if (profileComponent == null) return CompletableFuture.completedFuture(Optional.empty());

        return MinecraftClient.getInstance()
                .getSkinProvider()
                .fetchSkinTextures(profileComponent.getGameProfile());
    }

    public static ProfileComponent minimizeTextures(GameProfile profile) {
        ProfileComponent profileComponent = ProfileComponent.ofStatic(profile);
        Optional<String> unwrappedUrl = unwrapUrl(profileComponent);
        if (unwrappedUrl.isEmpty()) return profileComponent;

        Multimap<String, Property> properties = ImmutableMultimap.of("textures", new Property("textures", wrapUrl(unwrappedUrl.get())));
        PropertyMap propertiesMap = new PropertyMap(properties);

        profile = new GameProfile(profile.id(), profile.name(), propertiesMap);

        return ProfileComponent.ofStatic(profile);
    }

    public static Optional<String> unwrapUrl(ProfileComponent profileComponent) {
        List<Property> texturesProperties = profileComponent.getGameProfile().properties().get("textures").stream().toList();

        if (texturesProperties.isEmpty()) return Optional.empty();

        Optional<String> textureValueOptional = TextUtils.decodeBase64(texturesProperties.get(0).value());
        if (textureValueOptional.isEmpty()) return Optional.empty();

        return unwrapUrl(textureValueOptional.get());
    }

    public static Optional<String> unwrapUrl(String skinValue) {
        try {
            Optional<String> textureValueOptional = TextUtils.decodeBase64(skinValue);
            if (textureValueOptional.isEmpty()) return Optional.empty();

            JsonObject json = JsonParser.parseString(textureValueOptional.get()).getAsJsonObject();
            if (!json.has("textures") || !json.get("textures").isJsonObject()) return Optional.empty();

            JsonObject textures = json.getAsJsonObject("textures");
            if (!textures.has("SKIN") || !textures.get("SKIN").isJsonObject()) return Optional.empty();

            json = textures.get("SKIN").getAsJsonObject();
            if (!json.has("url") || !json.get("url").isJsonPrimitive()) return Optional.empty();

            JsonPrimitive jsonUrl = json.getAsJsonPrimitive("url");
            if (!jsonUrl.isString()) return Optional.empty();

            return Optional.of(jsonUrl.getAsString());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public static String wrapUrl(String url) {
        JsonObject skin = new JsonObject();
        skin.addProperty("url", url);

        JsonObject textures = new JsonObject();
        textures.add("SKIN", skin);

        JsonObject json = new JsonObject();
        json.add("textures", textures);

        return TextUtils.encodeBase64(json.toString());
    }

    public static ItemStack dynamicHead(String name) {
        ItemStack result = Items.PLAYER_HEAD.getDefaultStack();

        result.apply(DataComponentTypes.PROFILE, null, profileComponent -> ProfileComponent.ofDynamic(name));

        return result;
    }
}
