package fzmm.zailer.me.client.logic.headGenerator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelArea;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelData;
import io.wispforest.owo.ui.core.Color;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ModelFillColorStep implements IModelStep {

    private final ModelArea area;

    public ModelFillColorStep(ModelArea area) {
        this.area = area;
    }

    @Override
    public void apply(ModelData data) {
        Color selectedColor = data.selectedColor().get();
        BufferedImage texture = data.textures().get("destination_skin");
        Graphics2D graphics = data.graphics();
        float red = selectedColor.red();
        float green = selectedColor.green();
        float blue = selectedColor.blue();

        // Multiply the color channels of each pixel by the corresponding color channels of the selected color
        int posX = this.area.getXWithOffset();
        int posY = this.area.getYWithOffset();
        int posX2 = posX + this.area.width();
        int posY2 = posY + this.area.height();
        if (posX2 > texture.getWidth() && posY2 > texture.getHeight()) {
            FzmmClient.LOGGER.error("[ModelFillColorStep] Pixel outside of texture: " + posX2 + " " + posY2);
            return;
        }
        for (int y = posY; y < posY2; y++) {
            for (int x = posX; x < posX2; x++) {
                int rgba = texture.getRGB(x, y);
                int alpha = (rgba >> 24) & 0xFF;
                int origRed = (rgba >> 16) & 0xFF;
                int origGreen = (rgba >> 8) & 0xFF;
                int origBlue = rgba & 0xFF;

                int newRed = (int) (origRed * red);
                int newGreen = (int) (origGreen * green);
                int newBlue = (int) (origBlue * blue);

                graphics.setColor(new java.awt.Color(newRed, newGreen, newBlue, alpha));
                graphics.fillRect(x, y, 1, 1);
            }
        }
    }

    public static ModelFillColorStep parse(JsonObject jsonObject) {
        ModelArea area = ModelArea.parse(jsonObject.get("area").getAsJsonObject());

        return new ModelFillColorStep(area);
    }
}
