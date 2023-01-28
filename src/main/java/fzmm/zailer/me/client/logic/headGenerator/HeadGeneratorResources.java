package fzmm.zailer.me.client.logic.headGenerator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.headGenerator.model.*;
import fzmm.zailer.me.client.logic.headGenerator.model.steps.*;
import fzmm.zailer.me.client.logic.headGenerator.texture.HeadTextureEntry;
import fzmm.zailer.me.utils.ImageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class HeadGeneratorResources {
    public static final String HEADS_TEXTURES_FOLDER = "textures/heads";
    public static final String FZMM_MODELS_FOLDER = "fzmm_models";
    public static final String HEADS_MODELS_FOLDER = FZMM_MODELS_FOLDER + "/heads";

    public static Set<HeadTextureEntry> loadHeadsTextures() {
        Set<HeadTextureEntry> entries = new HashSet<>();
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        List<ResourcePackProfile> resourcePackProfileList = minecraftClient.getResourcePackManager().getEnabledProfiles().stream().toList();

        for (var resourcePackProfile : resourcePackProfileList) {
            try (var resourcePack = resourcePackProfile.createResourcePack()) {

                resourcePack.findResources(ResourceType.CLIENT_RESOURCES, FzmmClient.MOD_ID, HEADS_TEXTURES_FOLDER, (identifier, inputStreamInputSupplier) -> {
                    try {
                        InputStream inputStream = inputStreamInputSupplier.get();
                        BufferedImage nativeImage = ImageIO.read(inputStream);
                        String path = identifier.getPath();
                        String fileName = path.substring(HEADS_TEXTURES_FOLDER.length() + 1, path.length() - ".png".length());

                        entries.add(new HeadTextureEntry(nativeImage, toDisplayName(fileName), fileName));

                        inputStream.close();
                    } catch (IOException ignored) {
                    }

                });
            }
        }
        return entries;
    }

    private static String toDisplayName(String name) {
        String displayName = name.replaceAll("_", " ");
        String firstCharacter = String.valueOf(displayName.charAt(0));
        return displayName.replaceFirst(firstCharacter, firstCharacter.toUpperCase());
    }

    public static Set<HeadModelEntry> loadHeadsModels() {
        Set<HeadModelEntry> entries = new HashSet<>();
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        List<ResourcePackProfile> resourcePackProfileList = minecraftClient.getResourcePackManager().getEnabledProfiles().stream().toList();

        for (var resourcePackProfile : resourcePackProfileList) {
            try (var resourcePack = resourcePackProfile.createResourcePack()) {

                resourcePack.findResources(ResourceType.CLIENT_RESOURCES, FzmmClient.MOD_ID, HEADS_MODELS_FOLDER,
                        (identifier, inputStreamInputSupplier) -> {
                            try {
                                getHeadModel(identifier, inputStreamInputSupplier.get()).ifPresent(entries::add);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
        }
        return entries;
    }

    public static Optional<HeadModelEntry> getHeadModel(Identifier identifier, InputStream inputStream) {
        String path = identifier.getPath();
        try {
            String fileName = path.substring(HEADS_MODELS_FOLDER.length() + 1, path.length() - ".json".length());

            JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
            HashMap<String, BufferedImage> textures = getHeadModelTextures(jsonObject);

            JsonArray stepsArray = jsonObject.getAsJsonArray("steps");
            List<IModelStep> steps = new ArrayList<>();

            for (var element : stepsArray) {
                JsonObject stepObject = element.getAsJsonObject();

                switch (stepObject.get("type").getAsString()) {
                    case "select_texture" -> steps.add(ModelSelectTextureStep.parse(stepObject));
                    case "copy" -> steps.add(ModelCopyStep.parse(stepObject));
                    case "delete" -> steps.add(ModelDeleteStep.parse(stepObject));
                }
            }

            Optional<HeadModelEntry> entry = Optional.of(new HeadModelEntry(toDisplayName(fileName), fileName, steps, textures));

            inputStream.close();
            return entry;
        } catch (Exception e) {
            e.printStackTrace();
            assert MinecraftClient.getInstance().player != null;
            Text message = Text.translatable("fzmm.gui.headGenerator.model.error.loadingModel", path)
                    .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR));

            MinecraftClient.getInstance().player.sendMessage(message);
        }
        return Optional.empty();
    }

    private static HashMap<String, BufferedImage> getHeadModelTextures(JsonObject jsonObject) {
        HashMap<String, BufferedImage> result = new HashMap<>();
        if (!jsonObject.has("textures"))
            return result;

        JsonArray texturesArray = jsonObject.get("textures").getAsJsonArray();

        for (var textureElement : texturesArray) {
            JsonObject textureObject = textureElement.getAsJsonObject();
            String path = textureObject.get("path").getAsString();
            Identifier textureIdentifier = new Identifier(textureObject.get("path").getAsString());
            BufferedImage texture = ImageUtils.getImageFromIdentifier(textureIdentifier).orElseThrow(() -> new NoSuchElementException(path));
            result.put(textureObject.get("id").getAsString(), texture);
        }

        return result;
    }
}
