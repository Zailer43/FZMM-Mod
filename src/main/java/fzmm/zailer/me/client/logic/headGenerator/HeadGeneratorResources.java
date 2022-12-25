package fzmm.zailer.me.client.logic.headGenerator;

import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;

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
                    .addTexture(headData.headSkin())
                    .getHeadTexture();
            skinWithHeadTextureList.add(new HeadData(skinWithHeadTexture, headData.displayName(), headData.key()));
        }

        return skinWithHeadTextureList;
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
                            String fileName = path.substring(HEADS_FOLDER.length() + 1, path.length() - 4);

                            headData.add(new HeadData(nativeImage, toDisplayName(fileName), fileName));
                        } catch (IOException ignored) {
                        }

                    });
                }
            }
        return headData;
    }

    private static String toDisplayName(String name) {
        String displayName = name.replaceAll("_", " ");
        String firstCharacter = String.valueOf(displayName.charAt(0));
        return displayName.replaceFirst(firstCharacter, firstCharacter.toUpperCase());
    }
}
