package fzmm.zailer.me.client.renderer.customSkin;

import fzmm.zailer.me.client.FzmmClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(value = EnvType.CLIENT)
public class CustomHeadEntityRenderer extends MobEntityRenderer<CustomHeadEntity, CustomHeadEntityModel> {
    public CustomHeadEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new CustomHeadEntityModel(ctx.getPart(FzmmClient.MODEL_CUSTOM_HEAD_LAYER)), 0.5f);
    }

    @Override
    protected void scale(CustomHeadEntity customHeadEntity, MatrixStack matrixStack, float f) {
        float value = 0.9375f;
        matrixStack.scale(value, value, value);
    }

    @Override
    public Identifier getTexture(CustomHeadEntity entity) {
        return entity.getTextures();
    }
}



