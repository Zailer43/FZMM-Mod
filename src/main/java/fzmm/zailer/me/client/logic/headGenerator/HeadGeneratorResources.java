package fzmm.zailer.me.client.logic.headGenerator;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HeadGeneratorResources {
    public static final String HEADS_FOLDER = "textures/heads";

    public static List<HeadData> getHeadTexturesOf(BufferedImage skinBase) {
        if (skinBase == null)
            return new ArrayList<>();

        List<HeadData> skinWithHeadTextureList = new ArrayList<>();

        for (var headName : getHeadsNames()) {
            BufferedImage headTexture = getTexture(headName);
            BufferedImage skinWithHeadTexture = new HeadGenerator(skinBase)
                    .addTexture(headTexture)
                    .getHeadTexture();
            skinWithHeadTextureList.add(new HeadData(skinWithHeadTexture, headName));
        }

        return skinWithHeadTextureList;
    }

    @Nullable
    public static BufferedImage getTexture(String textureName) {
        if (!getHeadsNames().contains(textureName))
            return null;

        Identifier textureIdentifier = getIdentifier(textureName);
        return FzmmUtils.getImageFromIdentifier(textureIdentifier);
    }

    public static Set<String> getHeadsNames() {
        Set<String> texturesPath = new HashSet<>();
        ResourcePackManager resourcePackManager = MinecraftClient.getInstance().getResourcePackManager();
        for (var resourcePackProfile : resourcePackManager.getEnabledProfiles()) {
            try (var resourcePack = resourcePackProfile.createResourcePack()) {
                texturesPath.addAll(
                        resourcePack
                                .findResources(ResourceType.CLIENT_RESOURCES, FzmmClient.MOD_ID, HEADS_FOLDER, identifier -> identifier.getPath().endsWith(".png"))
                                .stream().map(identifier -> {
                                    String path = identifier.getPath();
                                    return path.substring(HEADS_FOLDER.length() + 1, path.length() - 4);
                                })
                                .toList()
                );
            }
        }
        return texturesPath;
    }

    public static Identifier getIdentifier(String name) {
        return new Identifier(FzmmClient.MOD_ID, String.format("%s/%s.png", HEADS_FOLDER, name));
    }
}
