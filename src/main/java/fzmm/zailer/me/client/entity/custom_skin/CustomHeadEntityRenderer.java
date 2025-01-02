package fzmm.zailer.me.client.entity.custom_skin;

import fzmm.zailer.me.client.FzmmClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value = EnvType.CLIENT)
public class CustomHeadEntityRenderer extends MobEntityRenderer<CustomHeadEntity, CustomHeadEntityRenderState, CustomHeadEntityModel> {
    public CustomHeadEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new CustomHeadEntityModel(ctx.getPart(FzmmClient.MODEL_CUSTOM_HEAD_LAYER)), 0.5f);
    }

    @Override
    public CustomHeadEntityRenderState createRenderState() {
        return new CustomHeadEntityRenderState(DefaultSkinHelper.getSteve().texture());
    }

    @Override
    public void updateRenderState(CustomHeadEntity customHeadEntity, CustomHeadEntityRenderState state, float f) {
        super.updateRenderState(customHeadEntity, state, f);
        state.texture = customHeadEntity.skin().texture();
    }

    @Override
    protected void scale(CustomHeadEntityRenderState state, MatrixStack matrices) {
        float value = 0.9375f;
        matrices.scale(value, value, value);
    }

    @Override
    protected @Nullable RenderLayer getRenderLayer(CustomHeadEntityRenderState state, boolean showBody, boolean translucent, boolean showOutline) {
        return RenderLayer.getEntityTranslucent(state.texture);
    }

    @Override
    public Identifier getTexture(CustomHeadEntityRenderState state) {
        return state.texture;
    }
}



