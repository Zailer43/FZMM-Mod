package fzmm.zailer.me.client.entity.custom_skin;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelWithHead;

public class CustomHeadEntityModel extends EntityModel<CustomHeadEntityRenderState> implements ModelWithHead {

    private final ModelPart head;

    public CustomHeadEntityModel(ModelPart root) {
        super(root);
        this.head = root.getChild(EntityModelPartNames.HEAD);
        ModelPart hat = this.head.getChild(EntityModelPartNames.HAT);
        hat.visible = true;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        ModelPartData headModelPartData = modelPartData.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create()
                .uv(0, 0)
                .cuboid(-4.0f, 12.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.rotation(0.0f, 0.0f, 0.0f));

        headModelPartData.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create()
                .uv(32, 0)
                .cuboid(-4.0f, 12.0f, -4.0f, 8.0f, 8.0f, 8.0f, new Dilation(0.45f)), ModelTransform.NONE);

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(CustomHeadEntityRenderState state) {
        super.setAngles(state);
        this.head.pitch = state.pitch * (float) (Math.PI / 180.0);
        this.head.yaw = state.relativeHeadYaw * (float) (Math.PI / 180.0);
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }
}
