package fzmm.zailer.me.utils.skin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Optional;

public class GetSkinFromMojang extends GetSkinDecorator {

    public GetSkinFromMojang(GetSkinDecorator getSkinDecorator) {
        super(getSkinDecorator);
    }

    public GetSkinFromMojang() {
        super(null);
    }

    @Override
    public Optional<BufferedImage> getSkin(String playerName) throws IOException {
        String stringUuid = FzmmUtils.getPlayerUuid(playerName);
        try (var httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://sessionserver.mojang.com/session/minecraft/profile/" + stringUuid);

            httpGet.addHeader("content-statusType", "image/jpeg");

            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();
            if ((response.getStatusLine().getStatusCode() / 100) != 2)
                return super.getSkin(playerName);

            InputStream inputStream = resEntity.getContent();
            JsonObject obj = (JsonObject) JsonParser.parseReader(new InputStreamReader(inputStream));
            JsonObject properties = (JsonObject) obj.getAsJsonArray("properties").get(0);

            String valueJsonStr = new String(Base64.getDecoder().decode(properties.get("value").getAsString()));
            obj = (JsonObject) JsonParser.parseString(valueJsonStr);
            String skinUrl = obj.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();

            return ImageUtils.getImageFromUrl(skinUrl);
        }
    }

    @Override
    public Optional<ItemStack> getHead(String playerName) {
        ItemStack stack = HeadBuilder.of(playerName);

        NbtCompound nbt = stack.getOrCreateSubNbt(SkullItem.SKULL_OWNER_KEY);
        nbt = nbt.getCompound(TagsConstant.HEAD_PROPERTIES);
        NbtList textures = nbt.getList(TagsConstant.HEAD_PROPERTIES_TEXTURES, NbtElement.COMPOUND_TYPE);
        if (textures.isEmpty())
            return super.getHead(playerName);


        return Optional.of(HeadBuilder.of(playerName));
    }
}
