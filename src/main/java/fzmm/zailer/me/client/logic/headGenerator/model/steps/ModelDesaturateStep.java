package fzmm.zailer.me.client.logic.headGenerator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelArea;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelData;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class ModelDesaturateStep implements IModelStep {

    private final ModelArea area;

    public ModelDesaturateStep(ModelArea area) {
        this.area = area;
    }

    @Override
    public void apply(ModelData data) {
        Optional<BufferedImage> optionalTexture = data.getTexture("destination_skin");
        if (optionalTexture.isEmpty())
            return;

        ModelArea area = this.area.copyWithOffset(data.offsets());
        BufferedImage texture = optionalTexture.get();

        int posX = area.getXWithOffset();
        int posY = area.getYWithOffset();
        int posX2 = posX + area.width();
        int posY2 = posY + area.height();
        if (posX2 > texture.getWidth() && posY2 > texture.getHeight()) {
            FzmmClient.LOGGER.error("[ModelFillColorStep] Pixel outside of texture: " + posX2 + " " + posY2);
            return;
        }
        int textureWidth = texture.getWidth();
        int textureHeight = texture.getHeight();
        for (int y = 0; y < textureHeight; y++) {
            for (int x = 0; x < textureWidth; x++) {
                int rgba = texture.getRGB(x, y);
                int alpha = (rgba >> 24) & 0xFF;
                int red = (rgba >> 16) & 0xFF;
                int green = (rgba >> 8) & 0xFF;
                int blue = rgba & 0xFF;
                int average = (red + green + blue) / 3;

                int newRgba = (alpha << 24) | (average << 16) | (average << 8) | average;
                texture.setRGB(x, y, newRgba);
            }
        }
    }

    public static ModelDesaturateStep parse(JsonObject jsonObject) {
        ModelArea area = ModelArea.parse(jsonObject.get("area").getAsJsonObject());

        return new ModelDesaturateStep(area);
    }
}
