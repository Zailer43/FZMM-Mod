package fzmm.zailer.me.client.logic.headGenerator.model.steps;

import com.google.gson.JsonObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ModelSelectTextureStep implements IModelStep {

    private final String textureId;

    public ModelSelectTextureStep(String textureId) {
        this.textureId = textureId;
    }

    @Override
    public void apply(Graphics2D graphics, HashMap<String, BufferedImage> textures, AtomicReference<BufferedImage> selectedTexture) {
        selectedTexture.set(textures.get(this.textureId));
    }

    public static ModelSelectTextureStep parse(JsonObject jsonObject) {
        String textureId = jsonObject.get("texture_id").getAsString();

        return new ModelSelectTextureStep(textureId);
    }
}
