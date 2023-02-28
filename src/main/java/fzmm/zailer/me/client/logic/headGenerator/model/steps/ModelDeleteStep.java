package fzmm.zailer.me.client.logic.headGenerator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelArea;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelData;

import java.awt.*;

public class ModelDeleteStep implements IModelStep {

    private final ModelArea area;

    public ModelDeleteStep(ModelArea area) {
        this.area = area;
    }

    @Override
    public void apply(ModelData data) {
        Graphics2D graphics = data.graphics();
        graphics.setBackground(new Color(0, 0, 0, 0));
        graphics.clearRect(this.area.getXWithOffset(), this.area.getYWithOffset(), this.area.width(), this.area.height());
    }

    public static ModelDeleteStep parse(JsonObject jsonObject) {
        ModelArea area = ModelArea.parse(jsonObject.get("area").getAsJsonObject());

        return new ModelDeleteStep(area);
    }
}
