package fzmm.zailer.me.client.logic.headGenerator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelArea;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelData;
import fzmm.zailer.me.client.logic.headGenerator.model.steps.fillColor.IFillColorAlgorithm;
import io.wispforest.owo.ui.core.Color;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class ModelFillColorStep implements IModelStep {

    private final ModelArea area;
    private final IFillColorAlgorithm algorithm;

    public ModelFillColorStep(ModelArea area, IFillColorAlgorithm algorithm) {
        this.area = area;
        this.algorithm = algorithm;
    }

    @Override
    public void apply(ModelData data) {
        Optional<BufferedImage> optionalTexture = data.getTexture("destination_skin");
        if (optionalTexture.isEmpty())
            return;

        BufferedImage texture = optionalTexture.get();
        Color selectedColor = data.selectedColor().get();
        Graphics2D graphics = data.graphics();

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
                int pixelColor = texture.getRGB(x, y);
                java.awt.Color color = this.algorithm.getColor(selectedColor, pixelColor);

                graphics.setColor(color);
                graphics.fillRect(x, y, 1, 1);
            }
        }
    }

    public static ModelFillColorStep parse(JsonObject jsonObject) {
        ModelArea area = ModelArea.parse(jsonObject.get("area").getAsJsonObject());
        String algorithmString = jsonObject.get("algorithm").getAsString();
        IFillColorAlgorithm algorithm = switch (algorithmString) {
            case "solid" -> IFillColorAlgorithm.SOLID;
            case "multiply" -> IFillColorAlgorithm.MULTIPLY;
            default -> IFillColorAlgorithm.SOLID;
        };

        return new ModelFillColorStep(area, algorithm);
    }
}
