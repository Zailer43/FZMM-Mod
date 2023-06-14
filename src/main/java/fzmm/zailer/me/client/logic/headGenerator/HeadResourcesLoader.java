package fzmm.zailer.me.client.logic.headGenerator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.headGenerator.model.HeadModelEntry;
import fzmm.zailer.me.client.logic.headGenerator.model.parameters.IModelParameter;
import fzmm.zailer.me.client.logic.headGenerator.model.parameters.ModelParameter;
import fzmm.zailer.me.client.logic.headGenerator.model.parameters.OffsetParameter;
import fzmm.zailer.me.client.logic.headGenerator.model.parameters.ResettableModelParameter;
import fzmm.zailer.me.client.logic.headGenerator.model.steps.*;
import fzmm.zailer.me.client.logic.headGenerator.texture.HeadTextureEntry;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class HeadResourcesLoader implements SynchronousResourceReloader, IdentifiableResourceReloadListener {

    private static final List<AbstractHeadEntry> LOADED_RESOURCES = new ArrayList<>();
    public static final String HEADS_TEXTURES_FOLDER = "textures/heads";
    public static final String FZMM_MODELS_FOLDER = "fzmm_models";
    public static final String HEADS_MODELS_FOLDER = FZMM_MODELS_FOLDER + "/heads";

    public static List<AbstractHeadEntry> getPreloaded() {
        return List.copyOf(LOADED_RESOURCES);
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(FzmmClient.MOD_ID, "head-resources-loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        LOADED_RESOURCES.clear();

        LOADED_RESOURCES.addAll(loadHeadsModels(manager));
        LOADED_RESOURCES.addAll(loadHeadsTextures(manager));

        LOADED_RESOURCES.sort(Comparator.comparing(AbstractHeadEntry::isFirstResult)
                .reversed()
                .thenComparing(AbstractHeadEntry::getKey));

    }

    private static Set<HeadTextureEntry> loadHeadsTextures(ResourceManager manager) {
        Set<HeadTextureEntry> entries = new HashSet<>();

        manager.findResources(HEADS_TEXTURES_FOLDER, identifier -> identifier.getPath().endsWith(".png")).forEach(((identifier, resource) -> {
            try {
                InputStream inputStream = resource.getInputStream();
                BufferedImage nativeImage = ImageIO.read(inputStream);
                String path = identifier.getPath();
                String fileName = path.substring(HEADS_TEXTURES_FOLDER.length() + 1, path.length() - ".png".length());

                entries.add(new HeadTextureEntry(nativeImage, toDisplayName(fileName), fileName));

                inputStream.close();
            } catch (IOException e) {
                FzmmClient.LOGGER.error("[HeadResourcesLoader] Error loading head generator texture", e);
            }
        }));
        return entries;
    }

    private static String toDisplayName(String name) {
        String displayName = name.replaceAll("_", " ");
        String firstCharacter = String.valueOf(displayName.charAt(0));
        return displayName.replaceFirst(firstCharacter, firstCharacter.toUpperCase());
    }

    private static Set<HeadModelEntry> loadHeadsModels(ResourceManager manager) {
        Set<HeadModelEntry> entries = new HashSet<>();


        manager.findResources(HEADS_MODELS_FOLDER, identifier -> identifier.getPath().endsWith(".json")).forEach(((identifier, resource) -> {
            try {
                InputStream inputStream = resource.getInputStream();
                entries.add(getHeadModel(identifier, inputStream));
                inputStream.close();
            } catch (Exception e) {
                FzmmClient.LOGGER.error("[HeadResourcesLoader] Error loading head generator model: {}", identifier.getPath(),  e);

                if (MinecraftClient.getInstance().player != null) {
                    Text message = Text.translatable("fzmm.gui.headGenerator.model.error.loadingModel", identifier.getPath())
                            .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR));

                    MinecraftClient.getInstance().player.sendMessage(message);
                }
            }
        }));

        return entries;
    }

    public static HeadModelEntry getHeadModel(Identifier identifier, InputStream inputStream) {
        String path = identifier.getPath();
        String fileName = path.substring(HEADS_MODELS_FOLDER.length() + 1, path.length() - ".json".length());

        JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        List<ResettableModelParameter<BufferedImage, String>> textures = getHeadModelTextures(jsonObject);
        List<? extends IModelParameter<Color>> colors = getHeadModelColors(jsonObject);
        List<? extends IModelParameter<OffsetParameter>> offsets = getHeadModelOffsets(jsonObject);
        boolean isPaintableModel = jsonObject.has("paintable") && jsonObject.get("paintable").getAsBoolean();
        boolean isEditingSkinBody = jsonObject.has("is_editing_skin_body") && jsonObject.get("is_editing_skin_body").getAsBoolean();
        boolean isFirstResult = jsonObject.has("first_result") && jsonObject.get("first_result").getAsBoolean();

        JsonArray stepsArray = jsonObject.getAsJsonArray("steps");
        List<IModelStep> steps = new ArrayList<>();

        for (var element : stepsArray) {
            JsonObject stepObject = element.getAsJsonObject();
            String id = stepObject.get("type").getAsString();

            IModelStep step = switch (id) {
                case "copy" -> ModelCopyStep.parse(stepObject);
                case "delete" -> ModelDeleteStep.parse(stepObject);
                case "desaturate" -> ModelDesaturateStep.parse(stepObject);
                case "fill_color" -> ModelFillColorStep.parse(stepObject);
                case "select_color" -> ModelSelectColorStep.parse(stepObject);
                case "select_texture" -> ModelSelectTextureStep.parse(stepObject);
                case "toggle_offset" -> ModelToggleOffsetStep.parse(stepObject);
                case "select_destination" -> ModelSelectDestinationStep.parse(stepObject);
                default -> data -> FzmmClient.LOGGER.warn("[HeadResourcesLoader] Unknown model step type: {}", id);
            };

            steps.add(step);
        }

        HeadModelEntry entry = new HeadModelEntry(toDisplayName(fileName), fileName, steps, textures, colors, offsets);

        entry.isPaintable(isPaintableModel);
        entry.isEditingSkinBody(isEditingSkinBody);
        entry.isFirstResult(isFirstResult);

        return entry;
    }

    private static List<ResettableModelParameter<BufferedImage, String>> getHeadModelTextures(JsonObject jsonObject) {
        List<ResettableModelParameter<BufferedImage, String>> result = new ArrayList<>();
        if (!jsonObject.has("textures"))
            return new ArrayList<>();

        JsonArray texturesArray = jsonObject.get("textures").getAsJsonArray();

        for (var textureElement : texturesArray) {
            JsonObject textureObject = textureElement.getAsJsonObject();
            String id = textureObject.get("id").getAsString();
            boolean requested = !textureObject.has("requested") || textureObject.get("requested").getAsBoolean();
            String defaultValue = textureObject.has("path") ? textureObject.get("path").getAsString() : null;

            result.add(new ResettableModelParameter<>(id, null, defaultValue, requested));
        }

        return result;
    }

    private static List<? extends IModelParameter<Color>> getHeadModelColors(JsonObject jsonObject) {
        List<ModelParameter<Color>> result = new ArrayList<>();
        if (!jsonObject.has("colors"))
            return result;

        JsonArray colorsArray = jsonObject.get("colors").getAsJsonArray();

        for (var colorElement : colorsArray) {
            JsonObject colorObject = colorElement.getAsJsonObject();
            String id = colorObject.get("id").getAsString();
            boolean requested = !colorObject.has("requested") || colorObject.get("requested").getAsBoolean();

            Color color = Color.WHITE;
            if (colorObject.has("color_hex")) {
                String colorHex = colorObject.get("color_hex").getAsString();
                color = Color.ofRgb(Integer.decode(colorHex));
            }

            result.add(new ModelParameter<>(id, color, requested));
        }

        return result;
    }

    private static List<? extends IModelParameter<OffsetParameter>> getHeadModelOffsets(JsonObject jsonObject) {
        List<ModelParameter<OffsetParameter>> result = new ArrayList<>();
        if (!jsonObject.has("offsets"))
            return result;

        JsonArray offsetsArray = jsonObject.get("offsets").getAsJsonArray();

        for (var offsetElement : offsetsArray) {
            JsonObject offsetObject = offsetElement.getAsJsonObject();
            String id = offsetObject.get("id").getAsString();
            boolean requested = !offsetObject.has("requested") || offsetObject.get("requested").getAsBoolean();
            byte value = offsetObject.has("value") ? offsetObject.get("value").getAsByte() : 0;
            byte minValue = offsetObject.has("min_value") ? offsetObject.get("min_value").getAsByte() : 0;
            byte maxValue = offsetObject.has("max_value") ? offsetObject.get("max_value").getAsByte() : 8;
            boolean isXAxis = offsetObject.has("axis") && offsetObject.get("axis").getAsString().equalsIgnoreCase("X");
            boolean enabled = offsetObject.has("enabled") && offsetObject.get("enabled").getAsBoolean();

            result.add(new ModelParameter<>(id, new OffsetParameter(value, minValue, maxValue, isXAxis, enabled), requested));
        }

        return result;
    }
}
