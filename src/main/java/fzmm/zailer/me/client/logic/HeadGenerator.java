package fzmm.zailer.me.client.logic;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HeadGenerator {
    public static final String HEADS_FOLDER = "textures/heads";
    private final BufferedImage image;

    public HeadGenerator(BufferedImage image) {
        this.image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        this.addTexture(image, false);
    }

    public HeadGenerator addTexture(BufferedImage texture) {
        return this.addTexture(texture, true);
    }
    public HeadGenerator addTexture(BufferedImage texture, boolean hatLayer) {
        if (texture == null)
            return this;

        Graphics2D g2d = this.image.createGraphics();
        this.addLayer(g2d, texture, hatLayer);
        g2d.dispose();
        return this;
    }

    public HeadGenerator merge(List<BufferedImage> imageList) {
        for (var image : imageList)
            this.addTexture(image);

        return this;
    }


    private void addLayer(Graphics2D finalImageGraphics, BufferedImage newLayer, boolean hatLayer) {
        int width = hatLayer ? 64 : 32;
        finalImageGraphics.drawImage(newLayer, 0, 0, width, 16, 0, 0, width, 16, null);
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
        return new Identifier(FzmmClient.MOD_ID, HEADS_FOLDER + "/" + name + ".png");
    }

    @Nullable
    public BufferedImage getHeadTexture() {
        return this.image;
    }
}
