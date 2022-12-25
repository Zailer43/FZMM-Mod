package fzmm.zailer.me.utils;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.mixin.PlayerSkinTextureAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Optional;

public class ImageUtils {

    public static Optional<BufferedImage> getImageFromIdentifier(Identifier identifier) {
        try {
            Optional<Resource> imageResource = MinecraftClient.getInstance().getResourceManager().getResource(identifier);
            return imageResource.isEmpty() ? Optional.empty() : Optional.of(ImageIO.read(imageResource.get().getInputStream()));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<BufferedImage> getPlayerSkin(String name) throws IOException, NullPointerException, JsonIOException {
        Optional<BufferedImage> skin = getPlayerSkinFromCache(name);

        return skin.isEmpty() ? getPlayerSkinFromMojang(name) : skin;
    }

    public static Optional<BufferedImage> getPlayerSkinFromMojang(String name) throws IOException {
        String stringUuid = FzmmUtils.getPlayerUuid(name);
        try (var httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://sessionserver.mojang.com/session/minecraft/profile/" + stringUuid);

            httpGet.addHeader("content-statusType", "image/jpeg");

            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();
            if ((response.getStatusLine().getStatusCode() / 100) != 2)
                return Optional.empty();

            InputStream inputStream = resEntity.getContent();
            JsonObject obj = (JsonObject) JsonParser.parseReader(new InputStreamReader(inputStream));
            JsonObject properties = (JsonObject) obj.getAsJsonArray("properties").get(0);

            String valueJsonStr = new String(Base64.getDecoder().decode(properties.get("value").getAsString()));
            obj = (JsonObject) JsonParser.parseString(valueJsonStr);
            String skinUrl = obj.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();

            return getImageFromUrl(skinUrl);
        }
    }

    public static Optional<BufferedImage> getPlayerSkinFromCache(String name) throws IOException {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        ClientPlayNetworkHandler clientPlayNetworkHandler = client.player.networkHandler;
        PlayerListEntry playerListEntry = clientPlayNetworkHandler.getPlayerListEntry(name);
        if (playerListEntry == null)
            return Optional.empty();

        Identifier skinIdentifier = playerListEntry.getSkinTexture();
        AbstractTexture texture = client.getTextureManager().getTexture(skinIdentifier);
        // if the player is invisible the texture is not an instance of PlayerSkinTexture
        if (!(texture instanceof PlayerSkinTexture skinTexture))
            return Optional.empty();

        File skinFile = ((PlayerSkinTextureAccessor) skinTexture).getCacheFile();

        return Optional.of(getImageFromPath(skinFile.getPath()));
    }

    public static BufferedImage getImageFromPath(String path) throws IOException {
        File imgFile = new File(path);
        return ImageIO.read(imgFile);
    }

    public static Optional<BufferedImage> getImageFromUrl(String urlLocation) throws IOException {
        URL url = new URL(urlLocation);
        return Optional.ofNullable(ImageIO.read(url));
    }

    public static Optional<NativeImage> toNativeImage(BufferedImage image) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", stream);
            byte[] bytes = stream.toByteArray();

            ByteBuffer data = BufferUtils.createByteBuffer(bytes.length).put(bytes);
            data.flip();
            return Optional.of(NativeImage.read(data));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }
}
