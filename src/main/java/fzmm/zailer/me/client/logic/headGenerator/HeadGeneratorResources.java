package fzmm.zailer.me.client.logic.headGenerator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.headGenerator.model.HeadModelEntry;
import fzmm.zailer.me.client.logic.headGenerator.model.IModelStep;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelResizeStep;
import fzmm.zailer.me.client.logic.headGenerator.texture.HeadTextureEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HeadGeneratorResources {
    public static final String HEADS_TEXTURES_FOLDER = "textures/heads";
    public static final String HEADS_MODELS_FOLDER = "models/heads";

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

                resourcePack.findResources(ResourceType.CLIENT_RESOURCES, FzmmClient.MOD_ID, HEADS_MODELS_FOLDER, (identifier, inputStreamInputSupplier) -> {
                    String path = identifier.getPath();
                    try {
                        InputStream inputStream = inputStreamInputSupplier.get();
                        String fileName = path.substring(HEADS_MODELS_FOLDER.length() + 1, path.length() - ".json".length());

                        JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
                        boolean convertInSteveModel = jsonObject.get("convert_in_steve_model").getAsBoolean();
                        JsonArray stepsArray = jsonObject.getAsJsonArray("steps");
                        List<IModelStep> steps = new ArrayList<>();

                        for (var element : stepsArray) {
                            JsonObject stepObject = element.getAsJsonObject();

                            if (stepObject.get("type").getAsString().equals("resize"))
                                steps.add(ModelResizeStep.parse(stepObject));
                        }

                        entries.add(new HeadModelEntry(toDisplayName(fileName), fileName, steps, convertInSteveModel));

                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        assert MinecraftClient.getInstance().player != null;
                        Text message = Text.translatable("fzmm.gui.headGenerator.model.error.loadingModel", path)
                                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR));

                        MinecraftClient.getInstance().player.sendMessage(message);
                    }

                });
            }
        }
        return entries;
    }
}
