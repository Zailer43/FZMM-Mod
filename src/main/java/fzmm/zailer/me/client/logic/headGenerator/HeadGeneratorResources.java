package fzmm.zailer.me.client.logic.headGenerator;

import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public class HeadGeneratorResources {
    public static final String HEADS_FOLDER = "textures/heads";

    public static List<HeadData> getHeadTexturesOf(BufferedImage skinBase) {
        if (skinBase == null)
            return new ArrayList<>();

        List<HeadData> skinWithHeadTextureList = new ArrayList<>();

        for (var headData : loadHeads()) {
            BufferedImage skinWithHeadTexture = new HeadGenerator(skinBase)
                    .addTexture(headData.skin())
                    .getHeadTexture();
            skinWithHeadTextureList.add(new HeadData(skinWithHeadTexture, headData.name()));
        }

        return skinWithHeadTextureList;
    }

    public static Optional<BufferedImage> getTexture(String textureName) {
        Optional<HeadData> headDataOptional = loadHeads().stream().filter(headData -> headData.name().equals(textureName)).findFirst();
        return headDataOptional.map(HeadData::skin);
    }

    public static Set<HeadData> loadHeads() {
        Set<HeadData> headData = new HashSet<>();
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
            List<ResourcePackProfile> resourcePackProfileList = minecraftClient.getResourcePackManager().getEnabledProfiles().stream().toList();

            for (var resourcePackProfile : resourcePackProfileList) {
                try (var resourcePack = resourcePackProfile.createResourcePack()) {

                    resourcePack.findResources(ResourceType.CLIENT_RESOURCES, FzmmClient.MOD_ID, HEADS_FOLDER, (identifier, inputStreamInputSupplier) -> {
                        try {
                            BufferedImage nativeImage = ImageIO.read(inputStreamInputSupplier.get());
                            String path = identifier.getPath();
                            String headName = path.substring(HEADS_FOLDER.length() + 1, path.length() - 4);
                            headData.add(new HeadData(nativeImage, headName));
                        } catch (IOException ignored) {
                        }

                    });
                }
            }
        return headData;
    }

    public static Identifier getIdentifier(String name) {
        return new Identifier(FzmmClient.MOD_ID, String.format("%s/%s.png", HEADS_FOLDER, name));
    }
}
