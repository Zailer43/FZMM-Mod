package fzmm.zailer.me.client.logic.headGenerator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelData;
import fzmm.zailer.me.client.logic.headGenerator.model.parameters.ModelParameter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class ModelSelectDestinationStep implements IModelStep {

    private final String textureId;

    public ModelSelectDestinationStep(String textureId) {
        this.textureId = textureId;
    }

    @Override
    public void apply(ModelData data) {
        ModelParameter<BufferedImage> textureParameter = null;

        for (ModelParameter<BufferedImage> parameter : data.textures()) {
            if (parameter.id().equals(this.textureId)) {
                textureParameter = parameter;
                break;
            }
        }

        if (textureParameter == null) {
            FzmmClient.LOGGER.warn("[ModelSelectTextureStep] Could not find texture parameter '{}'", this.textureId);
        } else {
            Optional<BufferedImage> textureOptional = textureParameter.value();
            if (!textureParameter.isRequested() && textureOptional.isEmpty()) {
                //FzmmClient.LOGGER.warn("[ModelSelectTextureStep] Could not find texture '{}'", this.textureId);
                return;
            }

            BufferedImage destination = textureOptional.orElse(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
            Graphics2D destinationGraphics = destination.createGraphics();

            data.destinationGraphics().dispose();
            data.destinationGraphics(destinationGraphics);
            data.destinationId(this.textureId);
        }
    }

    public static ModelSelectDestinationStep parse(JsonObject jsonObject) {
        String textureId = jsonObject.get("texture_id").getAsString();

        return new ModelSelectDestinationStep(textureId);
    }
}
