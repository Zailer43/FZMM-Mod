package fzmm.zailer.me.client.logic.headGenerator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.headGenerator.model.ModelData;
import io.wispforest.owo.ui.core.Color;


public class ModelSelectColorStep implements IModelStep {

    private final String colorId;

    public ModelSelectColorStep(String colorId) {
        this.colorId = colorId;
    }

    @Override
    public void apply(ModelData data) {
        Color color = data.getColor(this.colorId);

        if (color == null) {
            color = Color.WHITE;
            FzmmClient.LOGGER.error("[ModelSelectColorStep] Could not find color '{}'", this.colorId);
        }

        data.selectedColor(color);
    }

    public static ModelSelectColorStep parse(JsonObject jsonObject) {
        String colorId = jsonObject.get("color_id").getAsString();

        return new ModelSelectColorStep(colorId);
    }
}
