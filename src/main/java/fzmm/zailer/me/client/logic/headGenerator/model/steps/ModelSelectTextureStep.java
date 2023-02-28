package fzmm.zailer.me.client.logic.headGenerator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelData;

import java.awt.image.BufferedImage;

public class ModelSelectTextureStep implements IModelStep {

    private final String textureId;

    public ModelSelectTextureStep(String textureId) {
        this.textureId = textureId;
    }

    @Override
    public void apply(ModelData data) {
        BufferedImage texture = data.textures().get(this.textureId);

        if (texture == null) {
            texture = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            FzmmClient.LOGGER.error("[ModelSelectColorStep] Could not find texture '{}'", this.textureId);
        }

        data.selectedTexture().set(texture);
    }

    public static ModelSelectTextureStep parse(JsonObject jsonObject) {
        String textureId = jsonObject.get("texture_id").getAsString();

        return new ModelSelectTextureStep(textureId);
    }
}
