package fzmm.zailer.me.client.renderer.customHead;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.math.MatrixStack;

public class CustomHeadEntityModel extends SinglePartEntityModel<CustomHeadEntity> implements ModelWithHead {

    private final ModelPart head;

    public CustomHeadEntityModel(ModelPart root) {
        super(RenderLayer::getEntityTranslucent);
        this.head = root.getChild(EntityModelPartNames.HEAD);
        ModelPart hat = this.head.getChild(EntityModelPartNames.HAT);
        hat.visible = true;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        ModelPartData headModelPartData = modelPartData.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create()
                .uv(0, 0)
                .cuboid(-4.0f, 12.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));

        headModelPartData.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create()
                .uv(32, 0)
                .cuboid(-4.0f, 12.0f, -4.0f, 8.0f, 8.0f, 8.0f, new Dilation(0.45f)), ModelTransform.NONE);

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(CustomHeadEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.head.pitch = headPitch * 0.017453292F;
        this.head.yaw = headYaw * 0.017453292F;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        ImmutableList.of(this.head).forEach((modelRenderer) -> modelRenderer.render(matrices, vertices, light, overlay, red, green, blue, alpha));
    }

    @Override
    public ModelPart getPart() {
        return this.head;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }
}
