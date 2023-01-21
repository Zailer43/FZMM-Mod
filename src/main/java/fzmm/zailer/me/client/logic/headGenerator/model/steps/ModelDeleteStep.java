package fzmm.zailer.me.client.logic.headGenerator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelArea;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ModelDeleteStep implements IModelStep {

    private final ModelArea area;

    public ModelDeleteStep(ModelArea area) {
        this.area = area;
    }

    @Override
    public void apply(Graphics2D graphics, HashMap<String, BufferedImage> textures, AtomicReference<BufferedImage> selectedTexture) {
        graphics.setBackground(new Color(0, 0, 0, 0));
        graphics.clearRect(this.area.getXWithOffset(), this.area.getYWithOffset(), this.area.width(), this.area.height());
    }

    public static ModelDeleteStep parse(JsonObject jsonObject) {
        ModelArea area = ModelArea.parse(jsonObject.get("area").getAsJsonObject());

        return new ModelDeleteStep(area);
    }
}
