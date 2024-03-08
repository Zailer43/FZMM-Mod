package fzmm.zailer.me.mixin.custom_model_data_editor;

import net.minecraft.client.render.model.json.ModelOverrideList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelOverrideList.BakedOverride.class)
public interface BakedOverrideAccessor {

    @Accessor
    ModelOverrideList.InlinedCondition[] getConditions();
}
