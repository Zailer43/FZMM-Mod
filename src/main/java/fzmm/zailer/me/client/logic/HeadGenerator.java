package fzmm.zailer.me.client.logic;

import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public final class HeadGenerator {
    public static final String HEADS_FOLDER = "textures/heads";
    private final BufferedImage playerSkin;
    private BufferedImage headTexture;

    public HeadGenerator(BufferedImage playerSkin) {
        this.playerSkin = playerSkin;
        this.headTexture = null;
    }

    public HeadGenerator addTexture(BufferedImage texture) {
        if (this.playerSkin == null || texture == null)
            return this;

        this.headTexture = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = this.headTexture.createGraphics();
        this.setLayer(g2d, this.playerSkin, 32);
        this.setLayer(g2d, texture, 64);
        g2d.dispose();
        return this;
    }


    private void setLayer(Graphics2D finalImageGraphics, BufferedImage newLayer, int width) {
        finalImageGraphics.drawImage(newLayer, 0, 0, width, 16, 0, 0, width, 16, null);
    }

    @Nullable
    public static BufferedImage getTexture(String textureName) {
        if (!getHeadsNames().contains(textureName))
            return null;

        Identifier textureIdentifier = getIdentifier(textureName);
        try {
            Resource textureResource = MinecraftClient.getInstance().getResourceManager().getResource(textureIdentifier);
            return ImageIO.read(textureResource.getInputStream());
        } catch (IOException e) {
            return null;
        }
    }

    public static Set<String> getHeadsNames() {
        Set<String> texturesPath = new HashSet<>();
        ResourcePackManager resourcePackManager = MinecraftClient.getInstance().getResourcePackManager();
        for (var resourcePackProfile : resourcePackManager.getEnabledProfiles()) {
            texturesPath.addAll(
                    resourcePackProfile
                            .createResourcePack()
                            .findResources(ResourceType.CLIENT_RESOURCES, FzmmClient.MOD_ID, HEADS_FOLDER, Integer.MAX_VALUE, (path) -> path.endsWith(".png"))
                            .stream().map(identifier -> {
                                String path = identifier.getPath();
                                return path.substring(HEADS_FOLDER.length() + 1, path.length() - 4);
                            })
                            .toList()
            );
        }
        return texturesPath;
    }

    public static Identifier getIdentifier(String name) {
        return new Identifier(FzmmClient.MOD_ID, HEADS_FOLDER + "/" + name + ".png");
    }

    @Nullable
    public BufferedImage getHeadTexture() {
        return this.headTexture;
    }
}
