package fzmm.zailer.me.mixin.custom_model_data_editor;

import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelOverrideList.class)
public interface ModelOverrideListAccessor {

    @Accessor
    ModelOverrideList.BakedOverride[] getOverrides();

    @Accessor
    Identifier[] getConditionTypes();
}
