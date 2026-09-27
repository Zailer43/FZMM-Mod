package fzmm.zailer.me.client.logic.head_generator.model;

import com.google.common.collect.ImmutableList;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.steps.IModelStep;
import fzmm.zailer.me.client.logic.head_generator.model.steps.ModelCopyStep;

import java.util.ArrayList;
import java.util.List;

public class InternalModels {

    public static HeadModelEntry OLD_FORMAT_TO_NEW_FORMAT;
    public static HeadModelEntry SLIM_TO_WIDE;
    public static HeadModelEntry WIDE_TO_SLIM;
    public static List<HeadModelEntry> ROTATE;
    public static HeadModelEntry ROTATE_IN_X_AXIS;
    public static HeadModelEntry ROTATE_IN_Y_AXIS;
    public static HeadModelEntry ROTATE_IN_Z_AXIS;


    private static HeadModelEntry load(String key) {
        AbstractHeadEntry entry = HeadResourcesLoader.getByPath(getPath(key)).orElseGet(() -> {
            FzmmClient.LOGGER.error("[InternalModels] Error loading '{}' model", HeadResourcesLoader.INTERNAL_MODELS_FOLDER + "/" + key);
            return new HeadModelEntry();
        });

        if (!(entry instanceof HeadModelEntry modelEntry)) {
            FzmmClient.LOGGER.error("[InternalModels] '{}' is not a HeadModelEntry", HeadResourcesLoader.INTERNAL_MODELS_FOLDER + "/" + key);
            return new HeadModelEntry();
        }

        return modelEntry;
    }

    private static HeadModelEntry reverseCopyStep(HeadModelEntry originalEntry, String key) {
        List<IModelStep> originalSteps = originalEntry.getSteps();
        List<IModelStep> stepsCopy = new ArrayList<>();


        for (int i = originalSteps.size() - 1; i >= 0; i--) {
            IModelStep step = originalSteps.get(i);

            if (step instanceof ModelCopyStep modelCopyStep) {
                step = modelCopyStep.reverseCopy();
            }

            stepsCopy.add(step);
        }

        HeadModelEntry result = originalEntry.copy(getPath(key));
        result.setSteps(stepsCopy);

        return result;
    }

    private static String getPath(String key) {
        return HeadResourcesLoader.INTERNAL_FOLDER + "/" + key;
    }

    public static void reload() {
        OLD_FORMAT_TO_NEW_FORMAT = load("old_format_to_new_format");
        SLIM_TO_WIDE = load("slim_to_wide");
        WIDE_TO_SLIM = reverseCopyStep(SLIM_TO_WIDE, "wide_to_slim");
        ROTATE_IN_X_AXIS = load("rotate_in_x_axis");
        ROTATE_IN_Y_AXIS = load("rotate_in_y_axis");
        ROTATE_IN_Z_AXIS = load("rotate_in_z_axis");
        ROTATE = ImmutableList.of(ROTATE_IN_X_AXIS, ROTATE_IN_Y_AXIS, ROTATE_IN_Z_AXIS);
    }
}
