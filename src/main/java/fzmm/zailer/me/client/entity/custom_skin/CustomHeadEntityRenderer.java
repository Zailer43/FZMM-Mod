package fzmm.zailer.me.client.entity.custom_skin;

import com.mojang.blaze3d.vertex.PoseStack;
import fzmm.zailer.me.client.FzmmClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value = EnvType.CLIENT)
public class CustomHeadEntityRenderer extends MobRenderer<CustomHeadEntity, CustomHeadEntityRenderState, CustomHeadEntityModel> {
    public CustomHeadEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CustomHeadEntityModel(ctx.bakeLayer(FzmmClient.MODEL_CUSTOM_HEAD_LAYER)), 0.5f);
    }

    @Override
    public CustomHeadEntityRenderState createRenderState() {
        return new CustomHeadEntityRenderState(DefaultPlayerSkin.getDefaultSkin().body().texturePath());
    }

    @Override
    public void extractRenderState(CustomHeadEntity customHeadEntity, CustomHeadEntityRenderState state, float f) {
        super.extractRenderState(customHeadEntity, state, f);
        state.texture = customHeadEntity.skin().body().texturePath();
    }

    @Override
    protected void scale(CustomHeadEntityRenderState state, PoseStack matrices) {
        float value = 0.9375f;
        matrices.scale(value, value, value);
    }

    @Override
    protected @Nullable RenderType getRenderType(CustomHeadEntityRenderState state, boolean showBody, boolean translucent, boolean showOutline) {
        return RenderTypes.entityTranslucent(state.texture);
    }

    @Override
    public Identifier getTextureLocation(CustomHeadEntityRenderState state) {
        return state.texture;
    }
}



